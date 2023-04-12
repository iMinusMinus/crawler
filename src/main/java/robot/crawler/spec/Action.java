package robot.crawler.spec;

public record Action(String id, String name, String type, String target,
                     String actionName,
                     String cookies, String[] cookieNames, /* cookie management */
                     String inputValue, /* input */
                     int deltaX, int deltaY, String scrollTo, /* scroll */
                     long minWaitTime, long maxWaitTime, String expectedCondition, String testValue /* wait condition */) implements Step {

    public enum Type {
        ADD_COOKIES("+cookies"),
        DELETE_COOKIE("-cookie"),
        CLEAN_COOKIE("-cookies"),
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
