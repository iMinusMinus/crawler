package robot.crawler.reactor;

import org.jsoup.nodes.Element;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

// TODO
public record JsoupContext() implements Context<Element> {

    @Override
    public void addElements(String id, List<Element> elements) {

    }

    @Override
    public List<WebElement> getElements(String id) {
        return null;
    }

    @Override
    public void addElement(String id, WebElement element) {

    }

    @Override
    public WebElement getElement(String id) {
        return null;
    }

    @Override
    public void addScreenshot(String id, byte[] data) {

    }

    @Override
    public void activeWindow(String windowHandle) {

    }

    @Override
    public String currentWindow() {
        return null;
    }

    @Override
    public String destroyWindow() {
        return null;
    }

    @Override
    public void snapshotElement(String window, Element element) {

    }

    @Override
    public WebElement currentElement(String window) {
        return null;
    }

    @Override
    public void restoreElement(String window) {

    }

    @Override
    public void pushResult(Object object) {

    }

    @Override
    public Object popResult() {
        return null;
    }

    @Override
    public void initialResult(List<Map<String, Object>> data) {

    }

    @Override
    public void fillResult(String key, Object value) {

    }

    @Override
    public List<Map<String, Object>> getResult() {
        return null;
    }
}
