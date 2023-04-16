package robot.crawler.reactor;

import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import robot.crawler.spec.Step;
import robot.crawler.spec.TaskExecutor;
import robot.crawler.spec.TaskSettingDefinition;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

public class JsoupTaskExecutor implements TaskExecutor {

    private Connection connection;

    private JsoupContext context;

    @Override
    public void setUp(TaskSettingDefinition settings) {
        connection = new HttpConnection();
        if (Proxy.Type.HTTP.name().equalsIgnoreCase(settings.proxyType())) {
            String[] proxy = settings.proxyValue().split(":");
            connection.proxy(proxy[0], Integer.parseInt(proxy[1]));
        } else if (Proxy.Type.SOCKS.name().equalsIgnoreCase(settings.proxyType())) {
            String[] proxy = settings.proxyValue().split(":");
            connection.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxy[0], Integer.parseInt(proxy[1]))));
        }
        if (settings.userAgent() != null) {
            connection.userAgent(settings.userAgent());
        }
        context = new JsoupContext();
    }

    @Override
    public List<Map<String, Object>> doExecute(String url, List<? extends Step> steps) {
//        connection.url(url).get();
        for (Step step : steps) {
            Step.Type type = Step.Type.getInstance(step.type());
            assert type != null;
            JsoupStepHandlerFactory.getHandler(connection, type).execute(context, step);
        }
        return context.getResult();
    }

    @Override
    public void tearDown() {
        connection = null;
    }
}
