package robot.crawler.spec;

/**
 * @param id 可将id和定位到的元素/元素集关联起来，供后续的Box/Action/Finder使用
 * @param escapeScope 当处于循环时，定位默认基于元素集下当前循环的元素，如果定位不属于当前元素，需设置为true
 * @param xpath xpath定位器
 * @param selector css选择器
 * @param multi 返回单个元素还是元素列表
 */
public record Locator(String id, String name, String type,
                      boolean escapeScope, String xpath, String selector, boolean multi) implements Step {
}
