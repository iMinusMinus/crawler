package robot.crawler.anti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AntiDianPingAntiCrawler {

    private static final String LEVEL_PREFIX = "star_";

    private static final String LEVEL_NOT = "star_sml";

    private static final String DIANPING_URL = "https://www.dianping.com/";

    public static final String SHOP_LEVEL_CONVERTER = "dianping.shopLevelConverter";

    public static final String CATEGORY_CONVERTER = "dianping.categoryConverter";

    public static final String SUB_INDUSTRY_CONVERTER = "dianping.subIndustryConverter";

    public static final String AREA_CONVERTER = "dianping.areaConverter";

    private static final Logger log = LoggerFactory.getLogger(AntiDianPingAntiCrawler.class);

    public static String shopLevelConverter(String cssClass) {
        String[] attributes = cssClass.split(" ");
        for (String attribute : attributes) {
            if (attribute.startsWith(LEVEL_PREFIX) && !LEVEL_NOT.equals(attribute)) {
                return new BigDecimal(attribute.substring(LEVEL_PREFIX.length())).divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP).toString();
            }
        }
        log.error("convert[{}] execute fail for value: {}", SHOP_LEVEL_CONVERTER, cssClass);
        return null;
    }

    public static String categoryConverter(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static String sunIndustryConverter(String url) {
        String tmp = url.substring(0, url.lastIndexOf("/"));
        return tmp.substring(tmp.lastIndexOf("/") + 1);
    }

    public static String areaConverter(String url) {
        return url.substring(DIANPING_URL.length());
    }

}
