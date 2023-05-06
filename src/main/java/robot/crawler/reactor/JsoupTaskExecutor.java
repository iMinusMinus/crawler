package robot.crawler.reactor;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import robot.crawler.spec.ForceStopException;
import robot.crawler.spec.Step;
import robot.crawler.spec.TaskExecutor;
import robot.crawler.spec.TaskSettingDefinition;
import robot.crawler.spec.VerifyStopException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsoupTaskExecutor implements TaskExecutor {

    protected Connection connection;

    protected Context<Element> context;

    protected boolean debug;

    protected Document doc;

    @Override
    public void setUp(TaskSettingDefinition settings) {
        debug = settings.debug();
        connection = new HttpConnection();
        connection.ignoreHttpErrors(true);
        if (settings.device() != null && settings.device().userAgent() != null) {
            connection.userAgent(settings.device().userAgent());
        }
        if (Proxy.Type.HTTP.name().equalsIgnoreCase(settings.proxyType())) {
            String[] proxy = settings.proxyValue().split(":");
            connection.proxy(proxy[0], Integer.parseInt(proxy[1]));
        } else if (Proxy.Type.SOCKS.name().equalsIgnoreCase(settings.proxyType())) {
            String[] proxy = settings.proxyValue().split(":");
            connection.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1]))));
        }
        context = new Context<>(true);
    }

    @Override
    public List<Map<String, Object>> doExecute(String url, List<? extends Step> steps) {
        try {
            // some site return status 200, but content was un-authorization
            Connection.Response response = connection.url(url).execute();
            if (response.statusCode() != 200) {
                log.error("request url:{}, status: {} - {}", url, response.statusCode(), response.statusMessage());
                throw new ForceStopException("request url not ok");
            }
            doc = response.parse();
            context.activeWindow(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        context.snapshotElement(url, doc);
        for (Step step : steps) {
            Step.Type type = Step.Type.getInstance(step.type());
            assert type != null;
            JsoupStepHandlerFactory stepHandlerFactory = Register.registerIfGetWindowObjectNotExist(context.currentWindow(), JsoupStepHandlerFactory.class,
                    ()-> new JsoupStepHandlerFactory(connection, (Document) context.currentElement(context.currentWindow())));
            stepHandlerFactory.getHandler(type).execute(context, step);
        }
        context.restoreElement(url);
        return context.getResult();
    }

    @Override
    public List<Map<String, Object>> doHandleException(RuntimeException e) {
        if (e instanceof ForceStopException || e instanceof VerifyStopException || connection == null) {
            throw e;
        }
        log.error(e.getMessage(), e);
        try {
            if (debug) {
                log.info("{}", Optional.ofNullable(context.currentElement(context.currentWindow()))
                        .map(Element::html).orElse(null));
            }
            return context.getResult();
        } catch (Exception ignore) {
            log.error(ignore.getMessage(), ignore);
            return Collections.emptyList();
        }
    }

    @Override
    public String currentUrl() {
        return Optional.ofNullable(context).map(Context::currentWindow).orElse(null);
    }

        @Override
    public void tearDown() {
        connection = null;
        Register.destroyAllWindow();
    }
}
