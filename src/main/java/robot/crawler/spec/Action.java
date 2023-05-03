package robot.crawler.spec;

/**
 *
 * @param target Action的target可以是Locator产生，也可以是Action产生（如切换window/tab依赖click/submit）
 * @param shortcut jsoup的等价webdriver捷径脚本
 * @param actionName @see Action.Type
 * @param cookies 登录后从浏览器复制出来，代码解析
 * @param cookieNames 需要删除的cookie名称
 * @param inputValue 当actionName为input时，需传入输入的值
 * @param deltaX 当actionName为scroll时，传入水平滚动偏移量
 * @param deltaY 当actionName为scroll时，传入垂直滚动偏移量
 * @param scrollTo 当actionName为scroll时，传入滚动到指定元素（需先locator）
 * @param minWaitTime 当actionName为wait时，传入最小等待时间
 * @param maxWaitTime 当actionName为wait时，传入最大等待时间
 * @param expectedCondition 当actionName为wait时，传入停止等待条件
 * @param testValue 当actionName为wait时，传入停止等待测试值
 * @param ignoreNotApply 当不符合时是否忽略而不抛出异常
 */
public record Action(String id, String name, String type, String target, String shortcut,
                     String actionName,
                     String cookies, String[] cookieNames, /* cookie management */
                     String inputValue, /* input */
                     int deltaX, int deltaY, String scrollTo, /* scroll */
                     long minWaitTime, long maxWaitTime, String expectedCondition, String testValue, /* wait condition */
                     boolean ignoreNotApply) implements Step {

    public enum Type {
        ADD_COOKIES("+cookies"),
        DELETE_COOKIE("-cookie"),
        CLEAN_COOKIE("-cookies"),
        NAVIGATE("navigate"),
        INPUT("input"),
        CLICK("click"),
        SCREENSHOT("screenshot"),
        SWITCH("switch"),
        CLOSE("close"),
        SCROLL("scroll"),
        WAIT("wait"),
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
