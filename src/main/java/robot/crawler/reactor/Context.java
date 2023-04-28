package robot.crawler.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class Context<E> {

    private static final Logger log = LoggerFactory.getLogger(Context.class);

    private final boolean cleanElementsAfterClose;

    public Context(boolean cleanElementsAfterClose) {
        this.cleanElementsAfterClose = cleanElementsAfterClose;
    }

    private final Map<String, List<E>> elementsMap = new ConcurrentHashMap<>();

    private final Map<String, E> elements = new ConcurrentHashMap<>();

    private final Map<String, byte[]> screenshots = new ConcurrentHashMap<>();

    private final Stack<String> windows = new Stack<>();

    private final Stack<Object> result = new Stack<>();

    private final Map<String, Stack<E>> scopes = new HashMap<>();

    private final Map<String, List<String>> windowElements = new ConcurrentHashMap<>();

    public void addElements(String id, List<E> elements) {
        windowElements.computeIfAbsent(windows.peek(), (s) -> new ArrayList<>()).add(id);
        List<E> oldElements = elementsMap.put(id, elements);
        if (oldElements != null) {
            log.warn("key[{}] elements exist!", id);
        }
    }

    public void removeElements(String id) {
        elementsMap.remove(id);
    }

    public List<E> getElements(String id) {
        return elementsMap.get(id);
    }

    public void addElement(String id, E element) {
        windowElements.computeIfAbsent(windows.peek(), (s) -> new ArrayList<>()).add(id);
        E oldElement = elements.put(id, element);
        if (oldElement != null) {
            log.warn("key[{}] element exist!", id);
        }
    }

    public void removeElement(String id) {
        elements.remove(id);
    }

    public E getElement(String id) {
        return elements.get(id);
    }

    public void addScreenshot(String id, byte[] data) {
        windowElements.computeIfAbsent(windows.peek(), (s) -> new ArrayList<>()).add(id);
        screenshots.put(id, data);
    }

    public byte[] removeScreenshot(String id) {
        return screenshots.remove(id);
    }

    public void activeWindow(String windowHandle) {
        windows.push(windowHandle);
    }

    public String currentWindow() {
        return windows.peek();
    }

    public String destroyWindow() {
        String id = windows.pop();
        if (cleanElementsAfterClose) {
            List<String> stored = Optional.ofNullable(windowElements.get(id)).orElse(Collections.emptyList());
            for (String key : stored) {
                removeElement(key);
                removeElements(key);
                removeScreenshot(key);
            }
        }
        scopes.remove(id);
        return id;
    }

    public void snapshotElement(String window, E webElement) {
        scopes.computeIfAbsent(window, (id) -> new Stack<>()).push(webElement);
    }

    public E currentElement(String window) {
        return scopes.get(window) == null || scopes.get(window).isEmpty() ? null : scopes.get(window).peek();
    }

    public void restoreElement(String window) {
        if (scopes.get(window) == null || scopes.get(window).isEmpty()) {
            return;
        }
        scopes.get(window).pop();
    }

    public void pushResult(Object object) {
        log.debug("push++, current type: {}", object != null ? object.getClass(): null);
        result.push(object);
    }

    public Object popResult() {
        log.debug("pop++");
        return result.pop();
    }

    public void initialResult(List<Map<String, Object>> data) {
        if (!result.isEmpty()) {
            throw new RuntimeException("result has been initialized!");
        }
        log.debug("push root list");
        result.push(data);
    }

    public void fillResult(String key, Object value) {
        Object obj = result.peek();
        if (obj instanceof Map object) {
            if (key == null) {
                log.warn("put value {} to map, but key is null", value);
            }
            object.put(key, value);
        } else if (obj instanceof List array) {
            if (key != null) {
                log.warn("add value {} to list, but key[{}] exist", value, key);
            }
            array.add(value);
        } else {
            throw new RuntimeException("program error");
        }
    }

    public List<Map<String, Object>> getResult() {
        if (result.size() == 1) {
            return (List<Map<String, Object>>) result.pop();
        } else {
            throw new RuntimeException("program error");
        }
    }
}
