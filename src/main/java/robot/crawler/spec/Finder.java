package robot.crawler.spec;

public record Finder(String id, String name, String type, String target,
                     String xpath, String selector, /* 元素内定位元素 */
                     String valueGetter, String attributeKey,  /* 获取文本或属性 */
                     String outputPropertyName, String outputPropertyPath, String outputValueType, String valueConverter /* cell */) implements Step {
    public enum ValueGetterType {
        TEXT("text"),
        ATTRIBUTE("attribute"),
        ;

        private final String value;

        ValueGetterType(String value) {
            this.value = value;
        }

        public static ValueGetterType getInstance(String value) {
            for (ValueGetterType instance : ValueGetterType.values()) {
                if (instance.value.equals(value)) {
                    return instance;
                }
            }
            return null;
        }
    }
}
