package robot.crawler.reactor;

import robot.crawler.anti.AntiDianPingAntiCrawler;

public abstract class Register {

    public static void initialize() {
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.SHOP_STAR_CONVERTER, AntiDianPingAntiCrawler::shopStarConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.SUB_INDUSTRY_CONVERTER, AntiDianPingAntiCrawler::sunIndustryConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.CATEGORY_CONVERTER, AntiDianPingAntiCrawler::categoryConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.AREA_CONVERTER, AntiDianPingAntiCrawler::areaConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.SCORE_CONVERTER, AntiDianPingAntiCrawler::scoreConverter);
        ConverterFactory.registerConverter(AntiDianPingAntiCrawler.REVIEW_QUANTITY_CONVERTER, AntiDianPingAntiCrawler::reviewQuantityConverter);
    }
}
