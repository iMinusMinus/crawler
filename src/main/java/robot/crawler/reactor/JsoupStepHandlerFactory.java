package robot.crawler.reactor;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import robot.crawler.spec.Action;
import robot.crawler.spec.Box;
import robot.crawler.spec.Finder;
import robot.crawler.spec.Locator;
import robot.crawler.spec.Step;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class JsoupStepHandlerFactory {

    private static final Map<Step.Type, StepHandler> handlers = new ConcurrentHashMap<>();

    public static StepHandler getHandler(Connection connection, Step.Type type) {
        return handlers.computeIfAbsent(type, (x) -> {
            final StepHandler stepHandler;
            switch (type) {
                case BOX -> stepHandler = new BoxHandler(connection);
                case LOCATOR -> stepHandler = new LocatorHandler(connection);
                case ACTION -> stepHandler = new ActionHandler(connection);
                case FINDER -> stepHandler = new FinderHandler(connection);
                default -> throw new IllegalArgumentException("unknown step type:" + type);
            }
            return stepHandler;
        });
    }

    private static class BoxHandler implements StepHandler<JsoupContext, Box, Element> {

        private final Connection connection;

        public BoxHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void handle(JsoupContext context, Box step) {
            // TODO
        }
    }

    private static class LocatorHandler implements StepHandler<JsoupContext, Locator, Element> {

        private final Connection connection;

        public LocatorHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void handle(JsoupContext context, Locator step) {
            // TODO
        }
    }

    private static class ActionHandler implements StepHandler<JsoupContext, Action, Element> {

        private final Connection connection;

        public ActionHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void handle(JsoupContext context, Action step) {
            Action.Type type = Action.Type.getInstance(step.actionName());
            assert type != null;
            switch (type) {
                case ADD_COOKIES -> {
                    String[] cookies = step.cookies().split("; ");
                    for (String cookie : cookies) {
                        connection.cookie(cookie.split("=")[0], cookie.split("=")[1]);
                    }
                }
                default -> throw new RuntimeException("unimplemented!");
            }
        }
    }

    private static class FinderHandler implements StepHandler<JsoupContext, Finder, Element> {

        private final Connection connection;

        public FinderHandler(Connection connection) {
            this.connection = connection;
        }

        @Override
        public void handle(JsoupContext context, Finder step) {
            // TODO
        }
    }
}
