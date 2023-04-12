package robot.crawler.reactor;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ObjectFactory {

    public static Object getObject(String type) {
        assert type != null;
        Object value;
        switch (type) {
            case "list":
            case "array":
                value = new ArrayList<>();
                break;
            case "object":
            case "map":
                value = new HashMap<>();
                break;
            default: throw new IllegalArgumentException("unknown object type: " + type);
        }
        return value;
    }

}
