package robot.crawler.spec;

/**
 * @param required 元素是否必须存在
 * @param xpath xpath选择器
 * @param selector cssSeletor选择器
 * @param valueGetter 值是从text害死从attribute获取
 * @param attributeKey 从哪个attribute获取
 * @param outputPropertyName 输出属性名称
 * @param outputPropertyPath 输出属性仿xpath/jsonpath定位
 * @param outputValueType 输出属性类型
 * @param valueConverter 输出属性值转换处理
 */
public record Finder(String id, String name, String type,
                     boolean required, String xpath, String selector, /* 元素内定位元素 */
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
