package robot.crawler.spec;

import java.util.Map;

/**
 * 任务设置
 * @param debug 开启debug模式会提供更多信息
 * @param browserName 浏览器类型
 * @param proxyType 代理类型
 * @param proxyValue 代理地址
 * @param proxyUser 代理用户名
 * @param proxyPassword 代理密码
 * @param arguments 启动参数
 * @param experimentalOptions 实验性参数
 * @param capabilities 浏览器参数
 */
public record TaskSettingDefinition(boolean debug, String browserName,
                                    String proxyType, String proxyValue, String proxyUser, String proxyPassword,
                                    String[] arguments, Map<String, Object> experimentalOptions, Map<String, Object> capabilities) {
}
