package robot.crawler.spec;

import java.util.List;

public record Box(String id, String name, String type, String target, List<Step> steps,
                  String outputPropertyName, String outputPropertyPath, String outputValueType) implements Step {

    public static final String ROOT_OBJECT_ID = "$";

    public static final String PROPERTY_NEST_PATH_SEPARATOR = ".";
}
