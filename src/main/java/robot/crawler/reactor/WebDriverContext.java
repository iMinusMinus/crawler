package robot.crawler.reactor;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public record WebDriverContext(boolean cleanElementsAfterClose) implements Context<WebElement> {

    private static final Logger log = LoggerFactory.getLogger(WebDriverContext.class);

    private static final Map<String, List<WebElement>> elementsMap = new ConcurrentHashMap<>();

    private static final Map<String, WebElement> elements = new ConcurrentHashMap<>();

    private static final Map<String, byte[]> screenshots = new ConcurrentHashMap<>();

    private static final Stack<String> windows = new Stack<>();

    private static final Stack<Object> result = new Stack<>();

    private static final Map<String, Stack<WebElement>> scopes = new HashMap<>();

    private static final Map<String, List<String>> windowElements = new ConcurrentHashMap<>();

    @Override
    public void addElements(String id, List<WebElement> elements) {
        windowElements.computeIfAbsent(windows.peek(), (s) -> new ArrayList<>()).add(id);
        List<WebElement> oldElements = elementsMap.put(id, elements);
        if (oldElements != null) {
            log.warn("key[{}] elements exist!", id);
        }
    }

    public void removeElements(String id) {
        elementsMap.remove(id);
    }

    @Override
    public List<WebElement> getElements(String id) {
        return elementsMap.get(id);
    }

    @Override
    public void addElement(String id, WebElement element) {
        windowElements.computeIfAbsent(windows.peek(), (s) -> new ArrayList<>()).add(id);
        WebElement oldElement = elements.put(id, element);
        if (oldElement != null) {
            log.warn("key[{}] element exist!", id);
        }
    }

    public void removeElement(String id) {
        elements.remove(id);
    }

    @Override
    public WebElement getElement(String id) {
        return elements.get(id);
    }

    @Override
    public void addScreenshot(String id, byte[] data) {
        windowElements.computeIfAbsent(windows.peek(), (s) -> new ArrayList<>()).add(id);
        screenshots.put(id, data);
    }

    public byte[] removeScreenshot(String id) {
        return screenshots.remove(id);
    }

    @Override
    public void activeWindow(String windowHandle) {
        windows.push(windowHandle);
    }

    @Override
    public String currentWindow() {
        return windows.peek();
    }

    @Override
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

    @Override
    public void snapshotElement(String window, WebElement webElement) {
        scopes.computeIfAbsent(window, (id) -> new Stack<>()).push(webElement);
    }

    @Override
    public WebElement currentElement(String window) {
        return scopes.get(window) == null || scopes.get(window).isEmpty() ? null : scopes.get(window).peek();
    }

    @Override
    public void restoreElement(String window) {
        if (scopes.get(window) == null || scopes.get(window).isEmpty()) {
            return;
        }
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
        if (!result.isEmpty()) {
            throw new RuntimeException("result has been initialized!");
        }
        result.push(data);
    }

    @Override
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

    @Override
    public List<Map<String, Object>> getResult() {
        if (result.size() == 1) {
            return (List<Map<String, Object>>) result.pop();
        } else {
            throw new RuntimeException("program error");
        }
    }
}
