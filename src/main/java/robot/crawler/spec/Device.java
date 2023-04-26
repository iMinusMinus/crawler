package robot.crawler.spec;

import java.math.BigDecimal;

/**
 * 设备信息
 * @param name 设备名称
 * @param width 宽度
 * @param height 高度
 * @param pixRatio 设备像素比
 * @param userAgent 用户代理
 * @param isMobile Mobile或Desktop
 * @param touchable 是否可触摸
 * @param brandName 品牌名称
 * @param brandVersion 品牌版本
 * @param architecture 架构: x86, armv8
 * @param platformName 平台名称: Windows, Android, iOS
 * @param platformVersion 平台版本
 * @param model 设备型号
 */
public record Device(String name, Integer width, Integer height, BigDecimal pixRatio,
                     String userAgent, Boolean isMobile, Boolean touchable,
                     String brandName, String brandVersion,
                     String fullBrowserVersion,
                     String platformName, String platformVersion, String architecture, String model) {

}
