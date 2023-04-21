package robot.crawler.reactor;

import java.util.List;
import java.util.Map;

public interface Context<E> {

    void addElements(String id, List<E> elements);

    List<E> getElements(String id);

    void addElement(String id, E element);

    E getElement(String id);

    void addScreenshot(String id, byte[] data);

    void activeWindow(String windowHandle);

    String currentWindow();

    String destroyWindow();

    void snapshotElement(String window, E element);

    E currentElement(String window);

    void restoreElement(String window);

    void pushResult(Object object);

    Object popResult();

    void initialResult(List<Map<String, Object>> data);


    void fillResult(String key, Object value);


    List<Map<String, Object>> getResult();
}
