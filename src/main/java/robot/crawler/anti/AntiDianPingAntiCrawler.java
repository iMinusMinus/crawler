package robot.crawler.anti;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import robot.crawler.reactor.HttpSupport;
import robot.crawler.spec.ForceStopException;
import robot.crawler.spec.VerifyStopException;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.Deflater;

public abstract class AntiDianPingAntiCrawler {

    private static final Logger log = LoggerFactory.getLogger(AntiDianPingAntiCrawler.class);

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private static final String PROTO_END = "//";

    public static final String DIANPING_HOST = "www.dianping.com";

    public static final String DIANPING_AUTH_DOMAIN = "verify.meituan.com";

    public static final String DIANPING_LOGIN_DOMAIN = "account.dianping.com";

    public static final String TITLE_TAG = "title";

    public static final String DIANPING_VERIFY_TITLE = "验证中心";

    public static final String DIANPING_LOGIN_INVALID_TITLE = "大众点评网";

    public static final String FORBIDDEN_TEXT = "403 Forbidden";

    public static final String SIGN = "sign";

    private static final String TCV_PROPERTY = "textCssVersion";

    private static final List<String> UUID_KEY = List.of("dper", "_hc.v", "_lxsdk_cuid");

    private static final int PARTNER = 150;

    private static final int OPTIMUS_CODE = 10;

    private static final int PC_PLATFORM = 1;

    private static final int  QQ_PLATFORM = 14;

    private static final int MICRO_MESSENGER_PLATFORM = 11;

    private static final int MOBILE_PLATFORM = 3;

    private static final ThreadLocal<Map<String, Object>> SHOP_CONFIG = new ThreadLocal<>();

    public static String normalizeUrl(String currentUrl, String url) {
        try {
            new URL(url);
            return url;
        } catch (MalformedURLException mue) {
            return currentUrl.substring(0, currentUrl.indexOf(PROTO_END)) + url;
        }
    }

    // /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(e) ? /MicroMessenger/.test(e) ? 11 : /QQ\/([\d\.]+)/.test(e) ? 14 : 3 : 1
    public static int platform(String userAgent) {
        return PC_PLATFORM;
    }

    // window.shop_config.textCssVersion
    public static String tcv() {
        Map<String, Object> map = SHOP_CONFIG.get();
        if (map == null || map.isEmpty()) {
            return null;
        }
        return (String) map.get(TCV_PROPERTY);
    }

    public static String uuid(String cookies) {
        final String[] uuid = new String[1];
        HttpSupport.handleCookies(cookies, (k, v) -> {
            if(UUID_KEY.contains(k) && uuid[0] == null) {
                uuid[0] = v;
            }
        });
        return uuid[0];
    }

    public static String token(Map<String, Object> map, String param) {
        String sign = sign(param);
        map.put(SIGN, sign);
        try {
            byte[] stringify = jsonMapper.writeValueAsBytes(map);
            return Base64.getEncoder().encodeToString(zip(stringify));
        } catch (Exception ignore) {
            log.error(ignore.getMessage(), ignore);
        }
        return null;
    }

    private static String sign(String queryString) {
        Map<String, String> map = new TreeMap<>();
        HttpSupport.parseQueryString(queryString, map);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(HttpSupport.KEY_VALUE_SEPARATOR).append(entry.getValue()).append(HttpSupport.QUERY_STRING_JOINER);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        try {
            byte[] stringify = jsonMapper.writeValueAsBytes(sb.toString());
            return Base64.getEncoder().encodeToString(zip(stringify));
        } catch (Exception ignore) {
            log.error(ignore.getMessage(), ignore);
        }
        return null;
    }

    private static byte[] zip(byte[] stringify) {
        Deflater deflater = new Deflater(); // level: -1, chunkSize: 16384, memLevel: 8, method: 8, strategy: 0, windowBits: 15
        deflater.setInput(stringify);
        deflater.finish();
        byte[] buffer = new byte[1024];
        byte[] result = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(stringify.length / 2)) {
            while (!deflater.finished()) {
                int write = deflater.deflate(buffer);
                baos.write(buffer, 0, write);
            }
            result = baos.toByteArray();;
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
        deflater.end();
        return result;
    }

    // 返回标题为"403 Forbidden"
    public static void handleForbidden(WebDriver webDriver, String ignore) {
        if (FORBIDDEN_TEXT.equals(webDriver.getTitle())) {
            log.error("exit program, current page is:\n{}", webDriver.getPageSource());
            throw new ForceStopException(FORBIDDEN_TEXT);
        }
    }

    public static void failIfBlock(Element element) {
        if (element instanceof Document doc) {
            String title = doc.title();
            if (DIANPING_VERIFY_TITLE.equals(title) || DIANPING_LOGIN_INVALID_TITLE.equals(title)) {
                log.error("jsoup请求被转向到认证界面：\n{}", doc.html());
                throw new VerifyStopException("fail as verify page display");
            } else if (FORBIDDEN_TEXT.equals(title)) {
                log.error("exit program, current page is:\n{}", doc.html());
                throw new ForceStopException(FORBIDDEN_TEXT);
            }
        }
    }

    // 跳转滑块验证码界面（验证通过后可继续浏览）
    public static void handleVerify(WebDriver webDriver, String expect) {
        int times = 0;
        String not = null;
        if (expect == null) {
            not = webDriver.getCurrentUrl();
        }
        while(times < 1024 && !is(webDriver, expect, not)) {
            try {
                Thread.sleep(3000);
                if (times % 100 == 0) {
                    log.info("请通过滑块验证，程序才能继续执行");
                }
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
            times++;
        }
    }

    private static boolean is(WebDriver webDriver, String expect, String not) {
        if (expect != null) {
            return normalizeUrl(webDriver.getCurrentUrl(), expect).equals(webDriver.getCurrentUrl());
        }
        return !normalizeUrl(webDriver.getCurrentUrl(), not).equals(webDriver.getCurrentUrl());
    }

}
