package robot.crawler.reactor;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

// TODO
public record JsoupContext() implements Context<Element> {

    private static Stack<String> urls = new Stack<>();

    private static Map<String, List<String>> urlIds = new ConcurrentHashMap<>();

    private static Map<String, List<Element>> elementsMap = new ConcurrentHashMap<>();

    private static Map<String, Element> elements = new ConcurrentHashMap<>();

    private static final Stack<Object> result = new Stack<>();

    private static final Map<String, Stack<Element>> scopes = new ConcurrentHashMap<>();

    @Override
    public void addElements(String id, List<Element> elements) {
        urlIds.computeIfAbsent(id, (k) -> new ArrayList<>()).add(id);
        elementsMap.put(id, elements);
    }

    @Override
    public List<Element> getElements(String id) {
        return elementsMap.get(id);
    }

    @Override
    public void addElement(String id, Element element) {
        urlIds.computeIfAbsent(id, (k) -> new ArrayList<>()).add(id);
        elements.put(id, element);
    }

    @Override
    public Element getElement(String id) {
        return elements.get(id);
    }

    @Override
    public void addScreenshot(String id, byte[] data) {

    }

    @Override
    public void activeWindow(String windowHandle) {
        urls.push(windowHandle);
    }

    @Override
    public String currentWindow() {
        return urls.peek();
    }

    @Override
    public String destroyWindow() {
        String url = urls.pop();
        List<String> ids = urlIds.remove(url);
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                elementsMap.remove(id);
                elements.remove(id);
            }
        }
        return url;
    }

    @Override
    public void snapshotElement(String window, Element element) {
        scopes.computeIfAbsent(window, (url) -> new Stack<>()).push(element);
    }

    @Override
    public Element currentElement(String window) {
        return scopes.get(window) == null || scopes.get(window).isEmpty() ? null : scopes.get(window).peek();
    }

    @Override
    public void restoreElement(String window) {
        scopes.get(window).pop();
    }

    @Override
    public void pushResult(Object object) {
        result.push(object);
    }

    @Override
    public Object popResult() {
        return result.pop();
    }

    @Override
    public void initialResult(List<Map<String, Object>> data) {
        if (result.isEmpty()) {
            result.push(data);
        } else {
            throw new RuntimeException("root object must initialize once");
        }
    }

    @Override
    public void fillResult(String key, Object value) {
        Object current = result.peek();
        if (current instanceof List list) {
            list.add(value);
        } else if (current instanceof Map map) {
            map.put(key, value);
        } else {
            throw new RuntimeException("program error");
        }
    }

    @Override
    public List<Map<String, Object>> getResult() {
        if (result.size() != 1) {
            throw new RuntimeException("program error");
        }
        return (List<Map<String, Object>>) result.pop();
    }
}
