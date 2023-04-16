package robot.crawler.reactor;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ObjectFactory {

    public static Object getObject(String type) {
        assert type != null;
        return switch (type) {
            case "list", "array" -> new ArrayList<>();
            case "object", "map" -> new HashMap<>();
            default -> throw new IllegalArgumentException("unknown object type: " + type);
        };
    }

}
