package robot.crawler.spec;

import java.math.BigDecimal;

public record Location(String cityName, String acceptLanguage,
                       BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy) {
}
