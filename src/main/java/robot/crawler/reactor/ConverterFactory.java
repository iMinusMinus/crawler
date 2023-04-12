package robot.crawler.reactor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConverterFactory {

    private static final Map<String, ValueConverter> converters = new ConcurrentHashMap<>();

    public static void registerConverter(String id, ValueConverter valueConverter) {
        converters.put(id, valueConverter);
    }

    public static ValueConverter getConverter(String valueType) {
        return converters.computeIfAbsent(valueType, (type) -> {
            ValueConverter converter;
            switch (type) {
                case "str":
                case "string":
                case "java.lang.String":
                    converter = (str) -> str;
                    break;
                case "int":
                case "java.lang.Integer":
                    converter = Integer::parseInt;
                    break;
                default: throw new IllegalArgumentException("unknown converter: " + type);
            }
            return converter;
        });
    }

}
