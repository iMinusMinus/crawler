package robot.crawler.spec;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = Locator.class, name = "locator"),
        @JsonSubTypes.Type(value = Action.class, name = "action"),
        @JsonSubTypes.Type(value = Finder.class, name = "finder"),
        @JsonSubTypes.Type(value = Box.class, name = "box")
})
public interface Step {
    /**
     * id需是编程语言合法命名字符组成，即不能是"http://", "https://"
     * @return 步骤id
     */
    String id();

    /**
     *
     * @return 步骤名称，注释用
     */
    String name();

    /**
     *
     * @return 步骤类型
     */
    String type();

    /**
     *
     * @return 步骤所在目标：webdriver（默认）或元素
     */
    String target();

    enum Type {
        BOX("box"), // 用于包含子步骤
        LOCATOR("locator"), // 用于定位元素，并保存id和元素关系，作为后续引用
        ACTION("action"), // 用于对浏览器或元素执行动作
        FINDER("finder"), // 用于提取内容或属性
        ;

        private final String value;

        public String getValue() {
            return value;
        }

        Type(String value) {
            this.value = value;
        }

        public static Type getInstance(String value) {
            for (Type instance : Type.values()) {
                if (instance.value.equals(value)) {
                    return instance;
                }
            }
            return null;
        }
    }
}
