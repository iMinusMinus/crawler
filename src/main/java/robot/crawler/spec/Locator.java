package robot.crawler.spec;

/**
 * @param xpath xpath定位器
 * @param selector css选择器
 * @param multi 返回单个元素还是元素列表
 */
public record Locator(String id, String name, String type, String target,
                      String xpath, String selector, boolean multi) implements Step {
}
