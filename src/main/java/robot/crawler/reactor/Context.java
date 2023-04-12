package robot.crawler.reactor;

import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

public interface Context<E> {

    void addElements(String id, List<E> elements);

    List<WebElement> getElements(String id);

    void addElement(String id, WebElement element);

    WebElement getElement(String id);

    void addScreenshot(String id, byte[] data);

    void activeWindow(String windowHandle);

    String destroyWindow();

    void snapshotElement(E element);

    WebElement currentElement();

    void restoreElement();

    void pushResult(Object object);

    Object popResult();

    void initialResult(List<Map<String, Object>> data);


    void fillResult(String key, Object value);


    List<Map<String, Object>> getResult();
}
