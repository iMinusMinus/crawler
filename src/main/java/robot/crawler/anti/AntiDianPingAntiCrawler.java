package robot.crawler.anti;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class AntiDianPingAntiCrawler {

    private static final Logger log = LoggerFactory.getLogger(AntiDianPingAntiCrawler.class);

    private static final String PROTO_END = "//";

    public static final String DIANPING_AUTH_DOMAIN = "verify.meituan.com";

    public static String normalizeUrl(String currentUrl, String url) {
        try {
            new URL(url);
            return url;
        } catch (MalformedURLException mue) {
            return currentUrl.substring(0, currentUrl.indexOf(PROTO_END)) + url;
        }
    }

    // TODO dianping 返回标题为"403 Forbidden"

    // 跳转滑块验证码界面（验证通过后可继续浏览）
    public static void handleVerify(WebDriver webDriver, String expect) {
        while(!expect.equals(webDriver.getCurrentUrl())) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
        }
    }
}
