package robot.crawler.spec;

/**
 * 任务设置
 * @param debug 开启debug模式出现异常不会退出webdriver
 * @param driverName webdriver类型
 * @param userAgent 用户代理
 * @param proxyType 代理类型
 * @param proxyValue 代理地址
 * @param proxyUser 代理用户名
 * @param proxyPassword 代理密码
 * @param arguments webdriver启动参数
 */
public record TaskSettingDefinition(boolean debug, String driverName, String userAgent,
                                    String proxyType, String proxyValue, String proxyUser, String proxyPassword,
                                    String[] arguments) {
}
