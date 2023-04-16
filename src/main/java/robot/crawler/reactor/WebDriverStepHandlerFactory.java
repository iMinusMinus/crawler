package robot.crawler.reactor;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class WebDriverStepHandlerFactory {

    private static final Map<Step.Type, StepHandler> handlers = new ConcurrentHashMap<>();

    public static StepHandler getHandler(WebDriver webDriver, Step.Type type) {
        return handlers.computeIfAbsent(type, (x) -> {
            final StepHandler stepHandler;
            switch (type) {
                case BOX -> stepHandler = new BoxHandler(webDriver);
                case LOCATOR -> stepHandler = new LocatorHandler(webDriver);
                case ACTION -> stepHandler = new ActionHandler(webDriver);
                case FINDER -> stepHandler = new FinderHandler(webDriver);
                default -> throw new IllegalArgumentException("unknown step type:" + type);
            }
            return stepHandler;
        });
    }

    private static class LocatorHandler implements StepHandler<WebDriverContext, Locator, WebElement> {

        private final WebDriver webDriver;

        LocatorHandler(WebDriver webDriver) {
            this.webDriver = webDriver;
        }

        @Override
        public void handle(WebDriverContext context, Locator step) {
            log.debug("handle locator step: {}", step);
            By locator;
            if (step.xpath() != null) {
                locator = By.xpath(step.xpath());
            } else if(step.selector() != null) {
                locator = By.cssSelector(step.selector());
            } else {
                throw new IllegalArgumentException("missing xpath/css selector for locator");
            }
            WebElement scope = context.currentElement(webDriver.getWindowHandle());
            boolean findInScope = scope != null && !step.escapeScope();
            List<WebElement> elements = findInScope ? scope.findElements(locator) : webDriver.findElements(locator);
            if (step.multi()) {
                context.addElements(step.id(),  elements);
            } else if (!elements.isEmpty()){
                context.addElement(step.id(), elements.get(0));
            }
        }
    }

    private static class ActionHandler implements StepHandler<WebDriverContext, Action, WebElement> {

        private final WebDriver webDriver;

        private final Map<String, Function<String, ExpectedCondition<Boolean>>> expectedConditions = new HashMap<>();

        private final Map<String, String> windows = new HashMap<>();

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

        @Override
        public void handle(WebDriverContext context, Action step) {
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
                    switch (target) {
                        case BACK -> webDriver.navigate().back();
                        case FORWARD -> webDriver.navigate().forward();
                        case REFRESH -> webDriver.navigate().refresh();
                        default -> webDriver.navigate().to(target);
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
                    new Actions(webDriver).click(clickable).perform();
                    Set<String> afterClick = webDriver.getWindowHandles();
                    afterClick.removeAll(beforeClick);
                    if (!afterClick.isEmpty()) {
                        windows.put(step.id(), afterClick.iterator().next());
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
                }
                case CLOSE -> webDriver.close();
                case SCROLL -> {
                    Actions actions = new Actions(webDriver);
                    if (step.target() != null) {
                        WebElement origin = context.getElement(step.target());
                        WheelInput.ScrollOrigin scrollOrigin = WheelInput.ScrollOrigin.fromElement(origin);
                        actions.scrollFromOrigin(scrollOrigin, step.deltaX(), step.deltaY()).perform();
                    } else if (step.scrollTo() != null){
                        WebElement scrollTo = context.getElement(step.scrollTo());
                        actions.scrollToElement(scrollTo).perform();
                    } else {
                        actions.scrollByAmount(step.deltaX(), step.deltaY()).perform();
                    }
                }
            }
        }
    }

    private static class FinderHandler implements StepHandler<WebDriverContext, Finder, WebElement> {
        private final WebDriver webDriver;

        FinderHandler(WebDriver webDriver) {
            this.webDriver = webDriver;
        }

        @Override
        public void handle(WebDriverContext context, Finder step) {
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
            if (scope == null && locator != null) {
                target = webDriver.findElements(locator).stream().findFirst().orElse(null);
            } else if (scope != null && locator != null) {
                target = scope.findElements(locator).stream().findFirst().orElse(null);
            } else {
                target = scope;
            }
            Finder.ValueGetterType type = Finder.ValueGetterType.getInstance(step.valueGetter());
            if (type == null) {
                log.warn("supported getter type: {}, '{}' was unknown", Arrays.toString(Finder.ValueGetterType.values()), step.valueGetter());
                throw new IllegalArgumentException("unknown value getter type: " + step.valueGetter());
            }
            String value = resolve(target, type, step);
            context.fillResult(step.outputPropertyName(), convert(value, step.valueConverter()));
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

    private static class BoxHandler implements StepHandler<WebDriverContext, Box, WebElement> {

        private final WebDriver webDriver;

        public BoxHandler(WebDriver webDriver) {
            this.webDriver = webDriver;
        }

        @Override
        public boolean beforeHandle(WebDriverContext context, Box step) {
            if (step.hook() != null && step.hook().doBefore() != null) {
                return (Boolean) WebDriverStepHookExecutor.execute(webDriver, context, step.hook().doBefore());
            }
            return true;
        }

        @Override
        public void handle(WebDriverContext context, Box step) {
            log.debug("handle box step: {}", step);
            List<WebElement> elements= context.getElements(step.target());
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
                } else if (step.outputValueType() != null){ // 属性类型为list/object
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
            } else if (webElement != null){ // XX详情
                if (step.outputValueType() != null){
                    Object value = ObjectFactory.getObject(step.outputValueType());
                    context.fillResult(step.outputPropertyName(), value);
                    context.pushResult(value);
                }
                handleSteps(context, step, webElement);
                if (step.outputValueType() != null){
                    context.popResult();
                }
            }
        }

        private void handleSteps(WebDriverContext context, Box step, WebElement element) {
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
                WebDriverStepHandlerFactory.getHandler(webDriver, type).execute(context, s);
            }
            if (!step.noPushToContext()) {
                context.restoreElement(windowHandleId);
            }
            if(step.wrap()) {
                context.fillResult(isRootObject ? null : step.outputPropertyName(), context.popResult());
            }
        }

        @Override
        public void onThrow(WebDriverContext context, Box step, Exception e) {
            if (step.hook() != null && step.hook().doThrowing() != null) {
                 WebDriverStepHookExecutor.execute(webDriver, context, step.hook().doThrowing());
                 return;
            }
            if ((!Box.ROOT_OBJECT_ID.equals(step.outputPropertyName()) && step.outputValueType() != null) || step.wrap()) {
                context.popResult();
            }
            throw new RuntimeException(e);
        }

    }
}
