package robot.crawler.spec;

import java.util.List;

/**
 * 子步骤包装器，处理类似元素有相同的步骤
 * @param id 仅作为标记
 * @param target 需先获取WebElement/List&#60;WebElement&#62;作为target
 * @param steps 子步骤
 * @param outputPropertyName 输出的属性名称，作为根对象则为"$"，作为map的元素则key不能为null，作为list的元素则key为null
 * @param outputPropertyPath 提示对象路径，仿xpath/jsonpath，不使用"[]"
 * @param outputValueType 输出属性的类型
 * @param wrap 输出属性为list时，是否使用map包装里面的内容
 */
public record Box(String id, String name, String type, String target, List<Step> steps,
                  String outputPropertyName, String outputPropertyPath, String outputValueType, boolean wrap) implements Step {

    public static final String ROOT_OBJECT_ID = "$";

    public static final String PROPERTY_NEST_PATH_SEPARATOR = ".";
}
