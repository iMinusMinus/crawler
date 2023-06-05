package robot.crawler.reactor;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import robot.crawler.anti.AntiDianPingAntiCrawler;
import robot.crawler.spec.Action;
import robot.crawler.spec.Box;
import robot.crawler.spec.Finder;
import robot.crawler.spec.ForceStopException;
import robot.crawler.spec.Locator;
import robot.crawler.spec.Step;
import robot.crawler.spec.VerifyStopException;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class JsoupStepHandlerFactory {

    private static final Map<String, Consumer<Element>> anti = new ConcurrentHashMap<>();

    public static void registerAnti(String domain, Consumer<Element> func) {
        anti.put(domain, func);
    }

    private final Connection connection;

    private final Document doc;

    private final BoxHandler boxHandler;

    private final LocatorHandler locatorHandler;

    private final ActionHandler actionHandler;

    private final FinderHandler finderHandler;

    public JsoupStepHandlerFactory(Connection connection, Document doc) {
        this.connection = connection;
        this.doc = doc;
        boxHandler = new BoxHandler();
        locatorHandler = new LocatorHandler();
        actionHandler = new ActionHandler();
        finderHandler = new FinderHandler();
    }

    public StepHandler<Context<Element>, Step, Element> getHandler(Step.Type type) {
            final StepHandler stepHandler;
            switch (type) {
                case BOX -> stepHandler = boxHandler;
                case LOCATOR -> stepHandler = locatorHandler;
                case ACTION -> stepHandler = actionHandler;
                case FINDER -> stepHandler = finderHandler;
                default -> throw new IllegalArgumentException("unknown step type:" + type);
            }
            return stepHandler;
    }

    private class BoxHandler implements StepHandler<Context<Element>, Box, Element> {

        @Override
        public boolean beforeHandle(Context<Element> context, Box step) {
            if (step.hook() != null && step.hook().doBefore() != null) {
                return (Boolean) JsoupStepHookExecutor.execute(connection, doc, context, step.hook().doBefore());
            }
            return true;
        }

        @Override
        public void handle(Context<Element> context, Box step) {
            List<Element> elements = context.getElements(step.target());
            Element element = context.getElement(step.target());
            if (elements == null) {
                elements = List.of(element);
            }
            boolean isRootObject = Box.ROOT_OBJECT_ID.equals(step.outputPropertyName());
            if (isRootObject) {
                List<Map<String, Object>> root = new ArrayList<>();
                context.initialResult(root);
            } else if (step.outputValueType() != null) {
                Object value = ObjectFactory.getObject(step.outputValueType());
                context.fillResult(step.outputPropertyName(), value);
                context.pushResult(value);
            }
            for (Element ele : elements) {
                if (step.wrap()) {
                    Map<String, Object> map = new HashMap<>();
                    context.pushResult(map);
                }
                if (!step.noPushToContext()) {
                    context.snapshotElement(context.currentWindow(), ele);
                }
                for (Step s : step.steps()) {
                    Step.Type type = Step.Type.getInstance(s.type());
                    assert type != null;
                    // always push document first! but after close stack top may be not document!
                    JsoupStepHandlerFactory stepHandlerFactory = Register.registerIfGetWindowObjectNotExist(context.currentWindow(), JsoupStepHandlerFactory.class,
                            ()-> new JsoupStepHandlerFactory(connection, (Document) context.currentElement(context.currentWindow())));
                    stepHandlerFactory.getHandler(type).execute(context, s);
                }
                if (!step.noPushToContext()) {
                    context.restoreElement(context.currentWindow());
                }
                if (step.wrap()) {
                    context.fillResult(isRootObject ? null : step.outputPropertyName(), context.popResult());
                }
            }
            if (!isRootObject && step.outputValueType() != null) {
                context.popResult();
            }
        }

    }

    private class LocatorHandler implements StepHandler<Context<Element>, Locator, Element> {

        @Override
        public void handle(Context<Element> context, Locator step) {
            Element scope = context.currentElement(context.currentWindow());
            if (step.escapeScope()) {
                scope = doc;
            }
            Elements located = step.xpath() != null ? scope.selectXpath(step.xpath()) : scope.select(step.selector());
            if (step.multi()) {
                context.addElements(step.id(), located.stream().toList());
            } else if (!located.isEmpty()){
                context.addElement(step.id(), located.get(0));
            }
        }
    }

    private class ActionHandler implements StepHandler<Context<Element>, Action, Element> {

        @Override
        public void handle(Context<Element> context, Action step) {
            if (step.shortcut() != null) {
                boolean replacement = (Boolean) JsoupStepHookExecutor.execute(connection, context, step.shortcut());
                if (replacement) {
                    return;
                }
            }
            Action.Type type = Action.Type.getInstance(step.actionName());
            assert type != null;
            switch (type) {
                case ADD_COOKIES -> HttpSupport.handleCookies(step.cookies(), connection::cookie);
                case NAVIGATE -> {
                    visitUrl(step.target(), context);
                }
                case CLICK -> {
                    Element clickable = context.getElement(step.target());
                    if (clickable != null) {
                        String href = clickable.attr("href");
                        if (!href.isEmpty()) {
                            String target = AntiDianPingAntiCrawler.normalizeUrl(context.currentWindow(), href);
                            visitUrl(target, context);
                        }
                    } else if (!step.ignoreNotApply()) {
                        throw new IllegalArgumentException("click target must exist");
                    }
                }
                case INPUT, SCREENSHOT, SCROLL, SWITCH, WAIT -> {
                    // NO-OP
                }
                case CLOSE -> context.destroyWindow();
                default -> throw new RuntimeException("unimplemented!");
            }
        }

        private void visitUrl(String target, Context<Element> context) {
            try {
                URL url = new URL(target);
                Connection.Response response = connection.url(url).execute();
                if (response.statusCode() != 200) {
                    log.error("request url: {}, status: {} - {}", url, response.statusCode(), response.statusMessage());
                    throw new ForceStopException("navigate to url not ok");
                }
                // TODO 被重定向，需要登录/滑块通过后，再度重定向回到初始请求页面
                if (!connection.request().url().toString().equals(target)) {
                    // https://account.dianping.com/pclogin?redir=https%3A%2F%2Fwww.dianping.com%2Fsearch%2Fkeyword%2F7%2F0_${keyword}?encoded
                    log.warn("url redirect to: {}", connection.request().url());
                }
                Document newContent = response.parse();
                Optional.ofNullable(anti.get(url.getHost()))
                        .orElse(e -> log.warn("no anti for host: '{}'", url.getHost()))
                        .accept(newContent);
                context.activeWindow(target);
                context.snapshotElement(context.currentWindow(), newContent);
            } catch (ForceStopException | VerifyStopException fse) {
                throw fse;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class FinderHandler implements StepHandler<Context<Element>, Finder, Element> {

        private static final String RAW_VALUE_PROPERTY_NAME_FMT = "__%1$s__";

        @Override
        public void handle(Context<Element> context, Finder step) {
            Element scope = context.currentElement(context.currentWindow());
            if (step.escapeScope()) {
                scope = doc;
            }
            Element target;
            if (step.xpath() != null || step.selector() != null) {
                Elements elements = step.xpath() != null ? scope.selectXpath(step.xpath()) : scope.select(step.selector());
                target = elements.isEmpty() ? null : elements.get(0);
            } else {
                target = scope;
            }
            if (step.required() && target == null) {
                throw new RuntimeException("required property cannot find on element");
            }
            Finder.ValueGetterType type = Finder.ValueGetterType.getInstance(step.valueGetter());
            assert type != null;
            String raw;
            if (target == null) {
                raw = null;
            } else {
                raw = type == Finder.ValueGetterType.TEXT ? target.text() : target.attr(step.attributeKey());
            }
            Object value = raw;
            if (step.valueConverter() != null) {
                value = ConverterFactory.getConverter(step.valueConverter()).convert(raw);
            }
            context.fillResult(step.outputPropertyName(), value);
            if (step.outputPropertyName() != null && !Objects.equals(value, raw)) {
                context.fillResult(RAW_VALUE_PROPERTY_NAME_FMT.formatted(step.outputPropertyName()), raw);
            }
        }
    }
}
