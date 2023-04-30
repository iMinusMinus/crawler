package robot.crawler.reactor;

import robot.crawler.anti.AntiDianPingAntiCrawler;

public abstract class Register {

    public static void initialize() {
        WebDriverStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_AUTH_DOMAIN,
                AntiDianPingAntiCrawler::handleVerify);
        WebDriverStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_LOGIN_DOMAIN,
                AntiDianPingAntiCrawler::handleVerify);
        WebDriverStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_HOST,
                AntiDianPingAntiCrawler::handleForbidden);
        JsoupStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_HOST,
                AntiDianPingAntiCrawler::failIfVerify);
    }
}
