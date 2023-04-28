package robot.crawler.reactor;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import robot.crawler.anti.AntiDianPingAntiCrawler;
import robot.crawler.spec.Action;
import robot.crawler.spec.Box;
import robot.crawler.spec.Finder;
import robot.crawler.spec.Locator;
import robot.crawler.spec.Step;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class JsoupStepHandlerFactory {

    private static final Map<Step.Type, StepHandler<Context<Element>, Step, Element>> handlers = new ConcurrentHashMap<>();

    private static final Map<String, Consumer<Element>> anti = new ConcurrentHashMap<>();

    public static void registerAnti(String domain, Consumer<Element> func) {
        anti.put(domain, func);
    }

    public static StepHandler<Context<Element>, Step, Element> getHandler(Connection connection, Document doc, Step.Type type) {
        return handlers.computeIfAbsent(type, (x) -> {
            final StepHandler stepHandler;
            switch (type) {
                case BOX -> stepHandler = new BoxHandler(connection, doc);
                case LOCATOR -> stepHandler = new LocatorHandler(connection, doc);
                case ACTION -> stepHandler = new ActionHandler(connection, doc);
                case FINDER -> stepHandler = new FinderHandler(doc);
                default -> throw new IllegalArgumentException("unknown step type:" + type);
            }
            return stepHandler;
        });
    }

    private record BoxHandler(Connection connection,
                              Document doc) implements StepHandler<Context<Element>, Box, Element> {

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
                    JsoupStepHandlerFactory.getHandler(connection, doc, type).execute(context, s);
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

    private record LocatorHandler(Connection connection,
                                  Document doc) implements StepHandler<Context<Element>, Locator, Element> {

        @Override
        public void handle(Context<Element> context, Locator step) {
            Element scope = context.currentElement(context.currentWindow());
            if (step.escapeScope()) {
                scope = doc;
            }
            Elements located = step.xpath() != null ? scope.selectXpath(step.xpath()) : scope.select(step.selector());
            if (step.multi()) {
                context.addElements(step.id(), located.stream().toList());
            } else {
                context.addElement(step.id(), located.get(0));
            }
        }
    }

    private record ActionHandler(Connection connection,
                                 Document doc) implements StepHandler<Context<Element>, Action, Element> {

        @Override
        public void handle(Context<Element> context, Action step) {
            Action.Type type = Action.Type.getInstance(step.actionName());
            assert type != null;
            switch (type) {
                case ADD_COOKIES -> {
                    String[] cookies = step.cookies().split("; ");
                    for (String cookie : cookies) {
                        connection.cookie(cookie.split("=")[0], cookie.split("=")[1]);
                    }
                }
                case NAVIGATE -> {
                    try {
                        URL url = new URL(step.target());
                        Element newContent = connection.url(url).get();
                        Optional.ofNullable(anti.get(url.getHost()))
                                .orElse(e -> log.warn("no anti for host: '{}'", url.getHost()))
                                .accept(newContent);
                        context.activeWindow(step.target());
                        context.snapshotElement(context.currentWindow(), newContent);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                case CLICK -> {
                    Element clickable = context.getElement(step.target());
                    String href = clickable.attr("href");
                    if (!href.isEmpty()) {
                        String target = AntiDianPingAntiCrawler.normalizeUrl(context.currentWindow(), href);
                        try {
                            URL url = new URL(target);
                            Element newContent = connection.url(target).get();
                            Optional.ofNullable(anti.get(url.getHost()))
                                    .orElse(e -> log.warn("no anti for host: '{}'", url.getHost()))
                                    .accept(newContent);
                            context.activeWindow(target);
                            context.snapshotElement(context.currentWindow(), newContent);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                case SCREENSHOT, SCROLL, SWITCH, WAIT -> {
                    // NO-OP
                }
                case CLOSE -> context.destroyWindow();
                default -> throw new RuntimeException("unimplemented!");
            }
        }
    }

    static class FinderHandler implements StepHandler<Context<Element>, Finder, Element> {

        private static final String RAW_VALUE_PROPERTY_NAME_FMT = "__%1$s__";

        private final Element doc;

        public FinderHandler(Document doc) {
            this.doc = doc;
        }

        @Override
        public void handle(Context<Element> context, Finder step) {
            Element scope = context.currentElement(context.currentWindow());
            if (step.escapeScope()) {
                scope = doc;
            }
            Element target;
            if (step.xpath() != null || step.selector() != null) {
                Elements elements = step.xpath() != null ? scope.selectXpath(step.xpath()) : scope.select(step.selector());
                target = elements.get(0);
            } else {
                target = scope;
            }
            if (step.required() && target == null) {
                throw new RuntimeException("required property cannot find on element");
            }
            Finder.ValueGetterType type = Finder.ValueGetterType.getInstance(step.valueGetter());
            assert type != null;
            String raw = type == Finder.ValueGetterType.TEXT ? target.text() : target.attr(step.attributeKey());
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
