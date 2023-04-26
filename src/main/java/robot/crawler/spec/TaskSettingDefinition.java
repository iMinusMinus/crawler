package robot.crawler.spec;

/**
 * 任务设置
 * @param debug 开启debug模式会提供更多信息
 * @param location 位置信息
 * @param device 设备信息
 * @param browserName 浏览器类型
 * @param proxyType 代理类型
 * @param proxyValue 代理地址
 * @param proxyUser 代理用户名
 * @param proxyPassword 代理密码
 * @param arguments 启动参数
 */
public record TaskSettingDefinition(boolean debug, Location location, Device device, String browserName,
                                    String proxyType, String proxyValue, String proxyUser, String proxyPassword,
                                    String[] arguments) {
}
