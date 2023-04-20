package robot.crawler.reactor;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robot.crawler.spec.Step;
import robot.crawler.spec.TaskExecutor;
import robot.crawler.spec.TaskSettingDefinition;

import java.util.List;
import java.util.Map;

public class WebDriverTaskExecutor implements TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(WebDriverTaskExecutor.class);

    private WebDriver webDriver;

    private WebDriverContext context;

    private boolean debug;

    @Override
    public void setUp(TaskSettingDefinition settings) {
        debug = settings.debug();
        AbstractDriverOptions options;
        WebDriver delegate;

        if (Browser.CHROME.is(settings.driverName())) {
            options = new ChromeOptions();
            configureOptions(options, settings);
            delegate = new ChromeDriver((ChromeOptions) options);
        } else if (Browser.EDGE.is(settings.driverName())) {
            options = new EdgeOptions();
            configureOptions(options, settings);
            delegate = new EdgeDriver((EdgeOptions) options);
        } else {
            throw new RuntimeException("当前仅支持chrome、edge，请联系开发者");
        }
        context = new WebDriverContext(true);
        webDriver = new EventFiringDecorator(new WebDriverWindowsEventListener(context), new WebDriverErrorEventListener())
                .decorate(delegate);
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
            WebDriverStepHandlerFactory.getHandler(webDriver, type).execute(context, step);
        }
        return context.getResult();
    }

    @Override
    public List<Map<String, Object>> doHandleException(RuntimeException e) {
        log.error(e.getMessage(), e);
        return context.getResult();
    }

    @Override
    public void tearDown() {
        if (!debug) {
            webDriver.quit();
        }
    }
}
