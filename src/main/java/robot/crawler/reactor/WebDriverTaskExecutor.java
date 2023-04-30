package robot.crawler.reactor;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v110.emulation.Emulation;
import org.openqa.selenium.devtools.v110.emulation.model.UserAgentBrandVersion;
import org.openqa.selenium.devtools.v110.emulation.model.UserAgentMetadata;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.events.EventFiringDecorator;
import robot.crawler.spec.Device;
import robot.crawler.spec.ForceStopException;
import robot.crawler.spec.Location;
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
            throw new RuntimeException("当前仅支持chrome、edge，请联系开发者");
        }
        context = new Context<>(true);
        webDriver = new EventFiringDecorator(new WebDriverWindowsEventListener(context), new WebDriverErrorEventListener())
                .decorate(delegate);
        if (webDriver instanceof ChromiumDriver chromium) {
            DevTools devTools = chromium.getDevTools();
            devTools.createSession();
//            devTools.addListener();
            Device device = settings.device();
            if (device != null ) {
                if (device.width() != null && device.height() != null
                        && device.pixRatio() != null && device.isMobile() != null) {
                    devTools.send(Emulation.setDeviceMetricsOverride(device.width(), device.height(), device.pixRatio(),
                            device.isMobile(),
                            Optional.empty(), Optional.empty(), Optional.empty(),
                            Optional.empty(), Optional.empty(),
                            Optional.empty(),
                            Optional.empty(), Optional.empty(), Optional.empty()));
                }
                UserAgentMetadata userAgentMetadata = null;
                if (device.platformName() != null && device.platformVersion() != null && device.architecture() != null
                        && device.model() != null && device.isMobile() != null) {
                    UserAgentBrandVersion brand = device.brandName() != null && device.brandVersion() != null ?
                            new UserAgentBrandVersion(device.brandName(), device.brandVersion()) : null;
                    userAgentMetadata = new UserAgentMetadata(brand != null ? Optional.of(List.of(brand)) : Optional.empty(),
                            Optional.empty(),
                            Optional.ofNullable(device.fullBrowserVersion()),
                            device.platformName(), device.platformVersion(),
                            device.architecture(),
                            device.model(), device.isMobile(), Optional.empty(), Optional.empty());
                }
                if (device.userAgent() != null) {
                    devTools.send(Emulation.setUserAgentOverride(device.userAgent(),
                            Optional.ofNullable(settings.location()).map(Location::acceptLanguage),
                            Optional.ofNullable(device.platformName()),
                            Optional.ofNullable(userAgentMetadata)));
                }
            }
            Location location = settings.location();
            if (location != null) {
                devTools.send(Emulation.setGeolocationOverride(Optional.ofNullable(location.latitude()),
                        Optional.ofNullable(location.longitude()),
                        Optional.ofNullable(location.accuracy())));
            }
        }
        // as webdriver inject to StepHandler, factory lifecycle keep same with webdriver, or
        // org.openqa.selenium.NoSuchSessionException: Session ID is null. Using WebDriver after calling quit()
        webDriverStepHandlerFactory = new WebDriverStepHandlerFactory();
        log.debug("webdriver[{}] set up success", webDriver);
    }

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
        if (options instanceof ChromiumOptions chromiumOptions
                && settings.arguments() != null && settings.arguments().length > 0) {
            chromiumOptions.addArguments(settings.arguments());
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
        if (e instanceof ForceStopException) {
            throw e;
        }
        try {
            if (debug) {
                log.info("{}", webDriver.getPageSource());
            }
            log.error(e.getMessage(), e);
            return context.getResult();
        } catch (Exception ignore) {
            log.warn(ignore.getMessage(), ignore);
            return Collections.emptyList();
        }
    }

    @Override
    public String currentUrl() {
        return webDriver.getCurrentUrl();
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
