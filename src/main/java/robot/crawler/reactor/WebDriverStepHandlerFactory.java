package robot.crawler.reactor;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import robot.crawler.spec.Action;
import robot.crawler.spec.Box;
import robot.crawler.spec.Finder;
import robot.crawler.spec.Locator;
import robot.crawler.spec.Step;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class WebDriverStepHandlerFactory {

    private final Map<Step.Type, StepHandler<Context<WebElement>, Step, WebElement>> handlers = new ConcurrentHashMap<>();

    private static final Map<String, BiConsumer<WebDriver, String>> anti = new ConcurrentHashMap<>();

    public WebDriverStepHandlerFactory() {

    }

    public static void registerAnti(String domain, BiConsumer<WebDriver, String> func) {
        anti.put(domain, func);
    }

    public StepHandler<Context<WebElement>, Step, WebElement> getHandler(WebDriver webDriver, Step.Type type) {
        return handlers.computeIfAbsent(type, (x) -> {
            final StepHandler stepHandler;
            switch (type) {
                case BOX -> stepHandler = new BoxHandler(webDriver, this);
                case LOCATOR -> stepHandler = new LocatorHandler(webDriver);
                case ACTION -> stepHandler = new ActionHandler(webDriver);
                case FINDER -> stepHandler = new FinderHandler(webDriver);
                default -> throw new IllegalArgumentException("unknown step type:" + type);
            }
            return stepHandler;
        });
    }

    private record LocatorHandler(
            WebDriver webDriver) implements StepHandler<Context<WebElement>, Locator, WebElement> {

        @Override
        public void handle(Context<WebElement> context, Locator step) {
            log.debug("handle locator step: {}", step);
            By locator;
            if (step.xpath() != null) {
                locator = By.xpath(step.xpath());
            } else if (step.selector() != null) {
                locator = By.cssSelector(step.selector());
            } else {
                throw new IllegalArgumentException("missing xpath/css selector for locator");
            }
            WebElement scope = context.currentElement(webDriver.getWindowHandle());
            boolean findInScope = scope != null && !step.escapeScope();
            List<WebElement> elements = findInScope ? scope.findElements(locator) : webDriver.findElements(locator);
            if (step.multi()) {
                context.addElements(step.id(), elements);
            } else if (!elements.isEmpty()) {
                context.addElement(step.id(), elements.get(0));
            }
        }
    }

    static class ActionHandler implements StepHandler<Context<WebElement>, Action, WebElement> {

        private final WebDriver webDriver;

        private final Map<String, Function<String, ExpectedCondition<Boolean>>> expectedConditions = new HashMap<>();

        private final Map<String, String> windows = new HashMap<>();

        private final Map<String, String> urls = new HashMap<>();

        private static final String BACK = "back";

        private static final String FORWARD = "forward";

        private static final String REFRESH = "refresh";

        ActionHandler(WebDriver webDriver) {
            this.webDriver = webDriver;
            expectedConditions.put("numberOfWindows", (expectWindows) -> ExpectedConditions.numberOfWindowsToBe(Integer.parseInt(expectWindows)));
            expectedConditions.put("elementPresence", (selector) -> driver -> !driver.findElements(By.cssSelector(selector)).isEmpty()
                    || !driver.findElements(By.xpath(selector)).isEmpty());
            expectedConditions.put("urlContains", ExpectedConditions::urlContains);
        }

        private String getDomainOfUrl(String currentUrl) {
            currentUrl = currentUrl.substring(currentUrl.indexOf("//") + 2);
            return currentUrl.substring(0, currentUrl.indexOf("/"));
        }

        @Override
        public void handle(Context<WebElement> context, Action step) {
            log.debug("handle action step: {}", step);
            Action.Type type = Action.Type.getInstance(step.actionName());
            if (type == null) {
                log.error("available action names: {}, given '{}'", Action.Type.values(), step.actionName());
                throw new IllegalArgumentException("unknown action name: " + step.actionName());
            }
            switch (type) {
                case ADD_COOKIES -> {
                    String[] cookies = step.cookies().split("; ");
                    for (String cookie : cookies) {
                        webDriver.manage().addCookie(new Cookie.Builder(cookie.split("=")[0], cookie.split("=")[1]).build());
                    }
                }
                case DELETE_COOKIE -> {
                    for (String cookieName : step.cookieNames()) {
                        webDriver.manage().deleteCookieNamed(cookieName);
                    }
                }
                case CLEAN_COOKIE -> webDriver.manage().deleteAllCookies();
                case NAVIGATE -> {
                    String target = step.target();
                    assert target != null;
                    boolean detectUrl = false;
                    switch (target) {
                        case BACK -> webDriver.navigate().back();
                        case FORWARD -> webDriver.navigate().forward();
                        case REFRESH -> webDriver.navigate().refresh();
                        default -> {
                            webDriver.navigate().to(target);
                            detectUrl = true;
                        }
                    }
                    String currentUrl = webDriver.getCurrentUrl();
                    if (detectUrl) {
                        Optional.ofNullable(anti.get(getDomainOfUrl(currentUrl)))
                                .orElse((driver, url) -> log.warn("No anti warranty for '{}'", url))
                                .accept(webDriver, target);
                    }
                    // 移除之前页面保存的信息
                    String windowId = context.currentWindow();
                    context.destroyWindow();
                    context.activeWindow(windowId);
                }
                case INPUT -> {
                    WebElement input = context.getElement(step.target());
                    new Actions(webDriver).sendKeys(input, step.inputValue()).perform();
                }
                case CLICK -> {
                    Set<String> beforeClick = webDriver.getWindowHandles();
                    WebElement clickable = context.getElement(step.target());
                    if (clickable != null) {
                        String href = clickable.getAttribute("href");
                        new Actions(webDriver).click(clickable).perform();
                        Set<String> afterClick = webDriver.getWindowHandles();
                        afterClick.removeAll(beforeClick);
                        if (!afterClick.isEmpty()) {
                            windows.put(step.id(), afterClick.iterator().next());
                            urls.put(step.id(), href);
                        }
                    } else if (!step.ignoreNotApply()) {
                        throw new IllegalArgumentException("click target must exist");
                    }
                }
                case SCREENSHOT -> {
                    WebElement capture = context.getElement(step.target());
                    if (capture == null) {
                        throw new IllegalArgumentException("cannot take screenshot as web element not found");
                    }
                    context.addScreenshot(step.id(), capture.getScreenshotAs(OutputType.BYTES));
                }
                case WAIT -> {
                    assert step.minWaitTime() <= step.maxWaitTime();
                    long waitTime = ThreadLocalRandom.current().nextLong(step.minWaitTime(), step.maxWaitTime());
                    Function<String, ExpectedCondition<Boolean>> expectedCondition = expectedConditions.get(step.expectedCondition());
                    if (expectedCondition == null) {
                        log.error("given ExpectedFunction: {}, current supported ExpectedFunction: {}", step.expectedCondition(), expectedConditions.keySet());
                        throw new IllegalArgumentException("unsupported ExpectedFunction id: " + step.expectedCondition());
                    }
                    try {
                        new FluentWait<>(webDriver).
                                withTimeout(Duration.ofMillis(waitTime))
                                .ignoring(NoSuchElementException.class)
                                .until(expectedCondition.apply(step.testValue()));
                    } catch (TimeoutException te) {
                        if (!step.ignoreNotApply()) {
                            throw te;
                        }
                    }
                }
                case SWITCH -> {
                    log.debug("opened windows/tabs: {}", webDriver.getWindowHandles());
                    log.debug("switch to window/tab: {}", step.target());
                    String windowToActive = windows.get(step.target());
                    if (windowToActive == null) {
                        throw new IllegalArgumentException("might bad window name/handle: " + step.target());
                    }
                    webDriver.switchTo().window(windowToActive);
                    context.activeWindow(webDriver.getWindowHandle());
                    String expectUrl = urls.remove(step.target());
                    String currentUrl = webDriver.getCurrentUrl();
                        Optional.ofNullable(anti.get(getDomainOfUrl(currentUrl)))
                                .orElse((driver, url) -> log.warn("No anti warranty for '{}'", url))
                                .accept(webDriver, expectUrl);
                }
                case CLOSE -> webDriver.close();
                case SCROLL -> {
                    Actions actions = new Actions(webDriver);
                    if (step.target() != null) {
                        WebElement origin = context.getElement(step.target());
                        WheelInput.ScrollOrigin scrollOrigin = WheelInput.ScrollOrigin.fromElement(origin);
                        actions.scrollFromOrigin(scrollOrigin, step.deltaX(), step.deltaY()).perform();
                    } else if (step.scrollTo() != null) {
                        WebElement scrollTo = context.getElement(step.scrollTo());
                        actions.scrollToElement(scrollTo).perform();
                    } else {
                        actions.scrollByAmount(step.deltaX(), step.deltaY()).perform();
                    }
                }
            }
        }
    }

    private record FinderHandler(WebDriver webDriver) implements StepHandler<Context<WebElement>, Finder, WebElement> {

        private static final String RAW_VALUE_PROPERTY_NAME_FMT = "__%1$s__";

        @Override
        public void handle(Context<WebElement> context, Finder step) {
            log.debug("handle finder step: {}", step);
            By locator;
            if (step.xpath() != null) {
                locator = By.xpath(step.xpath());
            } else if (step.selector() != null) {
                locator = By.cssSelector(step.selector());
            } else {
                locator = null;
            }
            WebElement scope = context.currentElement(webDriver.getWindowHandle());
            WebElement target;
            if ((scope == null || step.escapeScope()) && locator != null) {
                target = webDriver.findElements(locator).stream().findFirst().orElse(null);
            } else if (scope != null && locator != null) {
                target = scope.findElements(locator).stream().findFirst().orElse(null);
            } else {
                target = scope;
            }
            if (target == null && step.required()) {
                throw new RuntimeException("element not found, may be wrong target/scope, or loaded page content not your expect");
            }
            Finder.ValueGetterType type = Finder.ValueGetterType.getInstance(step.valueGetter());
            if (type == null) {
                log.warn("supported getter type: {}, '{}' was unknown", Arrays.toString(Finder.ValueGetterType.values()), step.valueGetter());
                throw new IllegalArgumentException("unknown value getter type: " + step.valueGetter());
            }
            String value = resolve(target, type, step);
            Object converted = convert(value, step.valueConverter());
            context.fillResult(step.outputPropertyName(), converted);
            if (step.outputPropertyName() != null && !Objects.equals(value, converted)) {
                context.fillResult(RAW_VALUE_PROPERTY_NAME_FMT.formatted(step.outputPropertyName()), value);
            }
        }

        private String resolve(WebElement element, Finder.ValueGetterType type, Finder hint) {
            if (element == null) {
                return null;
            }
            String value;
            switch (type) {
                case TEXT -> value = element.getText();
                case ATTRIBUTE -> value = element.getAttribute(hint.attributeKey());
                default -> value = null;
            }
            return value;
        }

        private Object convert(String raw, String converterId) {
            if (converterId == null || raw == null) {
                return raw;
            }
            return ConverterFactory.getConverter(converterId).convert(raw);
        }
    }

    static class BoxHandler implements StepHandler<Context<WebElement>, Box, WebElement> {

        protected final WebDriver webDriver;

        protected final WebDriverStepHandlerFactory webDriverStepHandlerFactory;

        public BoxHandler(WebDriver webDriver, WebDriverStepHandlerFactory webDriverStepHandlerFactory) {
            this.webDriver = webDriver;
            this.webDriverStepHandlerFactory = webDriverStepHandlerFactory;
        }

        @Override
        public boolean beforeHandle(Context<WebElement> context, Box step) {
            if (step.hook() != null && step.hook().doBefore() != null) {
                return (Boolean) WebDriverStepHookExecutor.execute(webDriver, context, step.hook().doBefore());
            }
            return true;
        }

        @Override
        public void handle(Context<WebElement> context, Box step) {
            log.debug("handle box step: {}", step);
            List<WebElement> elements = context.getElements(step.target());
            WebElement webElement = context.getElement(step.target());
            if (elements != null) { // XX列表
                boolean isRootObject = Box.ROOT_OBJECT_ID.equals(step.outputPropertyName());
                if (isRootObject) {
                    List<Map<String, Object>> root = new ArrayList<>();
                    context.initialResult(root);
                    // 不存在分页时处理
                    if (elements.isEmpty() && step.noPushToContext()) {
                        elements.add(null);
                    }
                } else if (step.outputValueType() != null) { // 属性类型为list/object
                    Object value = ObjectFactory.getObject(step.outputValueType());
                    context.fillResult(step.outputPropertyName(), value);
                    context.pushResult(value);
                }
                for (WebElement element : elements) {
                    handleSteps(context, step, element);
                }
                if (!isRootObject && step.outputValueType() != null) {
                    log.debug("pop result on step: {}", step);
                    context.popResult();
                }
            } else if (webElement != null) { // XX详情
                if (step.outputValueType() != null) {
                    Object value = ObjectFactory.getObject(step.outputValueType());
                    context.fillResult(step.outputPropertyName(), value);
                    context.pushResult(value);
                }
                handleSteps(context, step, webElement);
                if (step.outputValueType() != null) {
                    context.popResult();
                }
            }
        }

        private void handleSteps(Context<WebElement> context, Box step, WebElement element) {
            boolean isRootObject = Box.ROOT_OBJECT_ID.equals(step.outputPropertyName());
            if (step.wrap()) { // 子步骤的"outputPropertyName"不为null，box需要创建map把子步骤属性包起来
                Map<String, Object> object = new HashMap<>();
                context.pushResult(object);
            }
            String windowHandleId = webDriver.getWindowHandle();
            if (!step.noPushToContext()) {
                context.snapshotElement(windowHandleId, element);
            }
            for (Step s : step.steps()) {
                Step.Type type = Step.Type.getInstance(s.type());
                if (type == null) {
                    throw new IllegalArgumentException("step missing type: " + step);
                }
                getStepHandler(webDriver, type).execute(context, s);
            }
            if (!step.noPushToContext()) {
                context.restoreElement(windowHandleId);
            }
            if (step.wrap()) {
                context.fillResult(isRootObject ? null : step.outputPropertyName(), context.popResult());
            }
        }

        protected StepHandler<Context<WebElement>, Step, WebElement> getStepHandler(WebDriver webDriver, Step.Type type) {
            return webDriverStepHandlerFactory.getHandler(webDriver, type);
        }

        @Override
        public void onThrow(Context<WebElement> context, Box step, RuntimeException e) {
            if (step.hook() != null && step.hook().doThrowing() != null) {
                WebDriverStepHookExecutor.execute(webDriver, context, step.hook().doThrowing());
                return;
            }
            if ((!Box.ROOT_OBJECT_ID.equals(step.outputPropertyName()) && step.outputValueType() != null) || step.wrap()) {
                context.popResult();
            }
            throw e;
        }

    }
}
