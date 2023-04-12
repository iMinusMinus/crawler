package robot.crawler.reactor;

import robot.crawler.anti.AntiDianPingAntiCrawler;

public class Register {

    public static void initialize() {
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.SHOP_LEVEL_CONVERTER, AntiDianPingAntiCrawler::shopLevelConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.SUB_INDUSTRY_CONVERTER, AntiDianPingAntiCrawler::sunIndustryConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.CATEGORY_CONVERTER, AntiDianPingAntiCrawler::categoryConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.AREA_CONVERTER, AntiDianPingAntiCrawler::areaConverter);
    }
}
