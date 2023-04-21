package robot.crawler.reactor;

import org.codehaus.janino.ExpressionEvaluator;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConverterFactory {

    private static final Map<String, ValueConverter> converters = new ConcurrentHashMap<>();

    public static void registerConverter(String id, ValueConverter valueConverter) {
        converters.put(id, valueConverter);
    }

    public static ValueConverter getConverter(String valueType) {
        return converters.computeIfAbsent(valueType, (type) -> {
            ValueConverter converter = switch (type) {
                case "str", "string", "java.lang.String" -> (str) -> str;
                case "int", "java.lang.Integer" -> Integer::parseInt;
                case "number" -> (str) -> {
                    if (str.indexOf(".") > 0) {
                        return new BigDecimal(str);
                    } else {
                        return Long.parseLong(str);
                    }
                };
                default -> new ExpressionConverter(type);
            };
            return converter;
        });
    }

    private static class ExpressionConverter implements ValueConverter {

        private final String expression;

        public ExpressionConverter(String expression) {
            this.expression = expression;
        }

        @Override
        public Object convert(String raw) {
            try {
                ExpressionEvaluator ee = new ExpressionEvaluator();
                ee.setTargetVersion(8);
                ee.setSourceVersion(8);
                ee.setParameters(new String[]{"arg"}, new Class[]{String.class});
                ee.cook(expression);
                return ee.evaluate(raw);
            } catch (Exception e) {
                return null;
            }
        }
    }


}
