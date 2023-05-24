package robot.crawler.reactor;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.events.EventFiringDecorator;
import robot.crawler.spec.ForceStopException;
import robot.crawler.spec.Step;
import robot.crawler.spec.TaskExecutor;
import robot.crawler.spec.TaskSettingDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WebDriverTaskExecutor implements TaskExecutor {

    protected WebDriver webDriver;

    protected Context<WebElement> context;

    protected WebDriverStepHandlerFactory webDriverStepHandlerFactory;

    private boolean debug;

    @Override
    public void setUp(TaskSettingDefinition settings) {
        assert webDriver == null;
        debug = settings.debug();
        AbstractDriverOptions options;
        WebDriver delegate;

        if (Browser.CHROME.is(settings.browserName())) {
            options = new ChromeOptions();
            configureOptions(options, settings);
            delegate = new ChromeDriver((ChromeOptions) options);
        } else if (Browser.EDGE.is(settings.browserName())) {
            options = new EdgeOptions();
            configureOptions(options, settings);
            delegate = new EdgeDriver((EdgeOptions) options);
        } else {
            throw new IllegalArgumentException("当前仅支持chrome、edge，请联系开发者");
        }
        context = new Context<>(true);
        // devTools should be opened after get url, and command sent by devTools could not affect other window/tab
        webDriver = new EventFiringDecorator(new WebDriverWindowsEventListener(context), new WebDriverErrorEventListener())
                .decorate(delegate);
        // as webdriver inject to StepHandler, factory lifecycle keep same with webdriver, or
        // org.openqa.selenium.NoSuchSessionException: Session ID is null. Using WebDriver after calling quit()
        webDriverStepHandlerFactory = new WebDriverStepHandlerFactory();
        log.debug("webdriver[{}] set up success", webDriver);
    }

    /**
     * <a href="https://www.w3.org/TR/webdriver2/">W3C WebDriver</a>
     * <a href="https://www.selenium.dev/documentation/webdriver/drivers/options/">Browser Options</a>
     * <a href="https://peter.sh/experiments/chromium-command-line-switches/">args</a>
     * @param options webdriver options
     * @param settings user setting for webdriver options
     */
    private void configureOptions(AbstractDriverOptions options, TaskSettingDefinition settings) {
        Proxy proxy = new Proxy();
        if (java.net.Proxy.Type.HTTP.name().equalsIgnoreCase(settings.proxyType())) {
            proxy.setHttpProxy(settings.proxyValue());
        } else if (java.net.Proxy.Type.SOCKS.name().equalsIgnoreCase(settings.proxyType())) {
            proxy.setSocksProxy(settings.proxyValue());
            proxy.setSocksUsername(settings.proxyUser());
            proxy.setSocksPassword(settings.proxyPassword());
        } else {
            log.info("not set proxy or not find appropriate proxy type:" + settings.proxyType());
        }
        if (proxy.getProxyType() != Proxy.ProxyType.UNSPECIFIED) {
            options.setProxy(proxy);
        }
        if (options instanceof ChromiumOptions chromiumOptions) {
            if (settings.arguments() != null && settings.arguments().length > 0) {
                chromiumOptions.addArguments(settings.arguments());
            }
            if (settings.experimentalOptions() != null && !settings.experimentalOptions().isEmpty()) {
                for (Map.Entry<String, Object> entry : settings.experimentalOptions().entrySet()) {
                    chromiumOptions.setExperimentalOption(entry.getKey(), entry.getValue());
                }
            }
            if (settings.capabilities() != null && !settings.capabilities().isEmpty()) {
                for (Map.Entry<String, Object> entry : settings.capabilities().entrySet()) {
                    chromiumOptions.setCapability(entry.getKey(), entry.getValue());
                }
            }
            // android emulation requires adb
        }
    }

    @Override
    public List<Map<String, Object>> doExecute(String url, List<? extends Step> steps) {
        webDriver.get(url);

        for (Step step : steps) {
            Step.Type type = Step.Type.getInstance(step.type());
            if (type == null) {
                throw new IllegalArgumentException("step missing type: " + step);
            }
            webDriverStepHandlerFactory.getHandler(webDriver, type).execute(context, step);
        }
        return context.getResult();
    }

    @Override
    public List<Map<String, Object>> doHandleException(RuntimeException e) {
        if (e instanceof ForceStopException || webDriver == null) {
            throw e;
        }
        log.error(e.getMessage(), e);
        try {
            if (debug) {
                log.info("{}", webDriver.getPageSource());
            }
            return context.getResult();
        } catch (Exception ignore) {
            log.warn(ignore.getMessage(), ignore);
            return Collections.emptyList();
        }
    }

    @Override
    public String currentUrl() {
        return Optional.ofNullable(webDriver).map(WebDriver::getCurrentUrl).orElse(null);
    }

    @Override
    public void tearDown() {
        log.debug("destroy webdriver[{}]", webDriver);
        if (webDriver != null) {
            webDriver.quit();
        }
        context = null;
        webDriver = null;
        webDriverStepHandlerFactory = null;
    }
}
