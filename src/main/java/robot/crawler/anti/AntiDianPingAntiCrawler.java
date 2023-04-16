package robot.crawler.anti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AntiDianPingAntiCrawler {

    private static final String LEVEL_PREFIX = "star_";

    private static final String LEVEL_NOT = "star_sml";

    private static final String DIANPING_URL = "https://www.dianping.com/";

    public static final String SHOP_STAR_CONVERTER = "dianping.shopStarConverter";

    public static final String CATEGORY_CONVERTER = "dianping.categoryConverter";

    public static final String SUB_INDUSTRY_CONVERTER = "dianping.subIndustryConverter";

    public static final String AREA_CONVERTER = "dianping.areaConverter";

    public static final String SCORE_CONVERTER = "dianping.score";

    private static final String SCORE_SEPARATOR = "：";

    public static final String REVIEW_QUANTITY_CONVERTER = "dianping.reviewQuantity";

    private static final Logger log = LoggerFactory.getLogger(AntiDianPingAntiCrawler.class);

    @Deprecated
    public static String shopStarConverter(String cssClass) {
        String[] attributes = cssClass.split(" ");
        for (String attribute : attributes) {
            if (attribute.startsWith(LEVEL_PREFIX) && !LEVEL_NOT.equals(attribute)) {
                return new BigDecimal(attribute.substring(LEVEL_PREFIX.length())).divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP).toString();
            }
        }
        log.error("convert[{}] execute fail for value: {}", SHOP_STAR_CONVERTER, cssClass);
        return null;
    }

    @Deprecated
    public static String categoryConverter(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Deprecated
    public static String sunIndustryConverter(String url) {
        String tmp = url.substring(0, url.lastIndexOf("/"));
        return tmp.substring(tmp.lastIndexOf("/") + 1);
    }

    @Deprecated
    public static String areaConverter(String url) {
        return url.substring(DIANPING_URL.length());
    }

    @Deprecated
    public static String scoreConverter(String scoreText) {
        try {
            return scoreText.split(SCORE_SEPARATOR)[1];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return scoreText;
        }
    }

    @Deprecated
    public static String reviewQuantityConverter(String rawQuantity) {
        try {
            return rawQuantity.substring(1, rawQuantity.length() - 1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return rawQuantity;
        }
    }

    // TODO dianping 返回标题为"403 Forbidden"
}
