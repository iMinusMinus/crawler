package robot.crawler.spec;

public record TaskSettingDefinition(String driverName, String userAgent,
                                    String proxyType, String proxyValue, String proxyUser, String proxyPassword,
                                    String[] arguments) {
}
