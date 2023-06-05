package robot.crawler.anti;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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

    public static final String DIANPING_VERIFY_TITLE = "验证中心";

    public static final String DIANPING_LOGIN_INVALID_TITLE = "大众点评网";

    public static final String FORBIDDEN_TEXT = "403 Forbidden";

    public static final String SERVER_ERROR_TEXT = "HTTP ERROR 500";

    public static final String SIGN = "sign";

    private static final String TCV_PROPERTY = "textCssVersion";

    private static final List<String> UUID_KEY = List.of("dper", "_hc.v", "_lxsdk_cuid");

    private static final String BG_IMG_CSS_PROPERTY = "background-image";

    private static final String BG_SIZE_CSS_PROPERTY = "background-size";

    private static final String BG_IMG_DATA_PREFIX = "url(\"data:image/png;base64,";

    private static final String BG_IMG_DATA_SUFFIX = "\")";

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
            result = baos.toByteArray();
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
        } else if (DIANPING_HOST.equals(webDriver.getTitle())) {
            log.error("exit program, current page is:\n{}", webDriver.getPageSource());
            throw new ForceStopException(SERVER_ERROR_TEXT);
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
        long waitTimeInMilliseconds = 1800000; // XXX how long?
        long sleepTimeInMilliseconds = 6000;
        long notifyIntervalInMilliseconds = 5 * 60 * 1000;
        while(times < (waitTimeInMilliseconds / sleepTimeInMilliseconds) && !is(webDriver, expect, not)) {
            try {
                boolean cracked = tryCrackSlideCaptcha(webDriver);
                if (cracked || is(webDriver, expect, not)) {
                    log.info("滑块验证通过");
                    break;
                }
                Thread.sleep(sleepTimeInMilliseconds);
                if (times % (notifyIntervalInMilliseconds / sleepTimeInMilliseconds) == 0) {
                    log.warn("请通过滑块验证，程序才能继续执行:{}", webDriver.getCurrentUrl());
                }
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
            times++;
        }
        if (times >= (waitTimeInMilliseconds / sleepTimeInMilliseconds)) {
            throw new ForceStopException("waiting too long for verification, exit now");
        }
    }

    private static boolean tryCrackSlideCaptcha(WebDriver webDriver) {
        List<WebElement> puzzleSlideDrag = webDriver.findElements(By.id("puzzleSliderDrag"));
        if (!puzzleSlideDrag.isEmpty()) {
            WebElement sliderDrag = webDriver.findElement(By.id("puzzleSliderDrag")); // 滑动匹配图
            log.info("slider image background-size: {}", sliderDrag.getCssValue(BG_SIZE_CSS_PROPERTY));
            WebElement main = webDriver.findElement(By.id("puzzleImageMain")); // 背景图
            log.info("background image background-size: {}", main.getCssValue(BG_SIZE_CSS_PROPERTY));
            WebElement draggable = webDriver.findElement(By.id("puzzleSliderBox")); // puzzleSliderMoveingBar, 拖动条
            new Actions(webDriver).clickAndHold(draggable).perform();
            // 点评滑块人眼大小远小于占据大小（5.831em * 13.875em），背景图为18.5em * 13.875em = 296 * 222，font-size为16px，实际缩放成原图的1/3了！
            // 点评滑块每次视野位置不同，固定水平位移失效
            byte[] slideImg = parseImgFromElement(sliderDrag);
            Point offset = AntiCaptcha.edgeOffset(slideImg);
            AntiCaptcha.passThroughSlideCaptcha(webDriver, slideImg, parseImgFromElement(main), 1d/3d, -offset.x);
            new Actions(webDriver).release(draggable).perform();
            List<WebElement> successTips = webDriver.findElements(By.id("puzzleSliderSuccess"));
            if (!successTips.isEmpty() && successTips.get(0).isDisplayed()) {
                return true;
            }
            // XXX 是否主动刷新滑块，再次尝试
        }
        // 滑动到底，此方式要求滑动速度较快，否则在显示界面位置被重置到初始地方，webDriver仍在做无效滑动，即便多次尝试滑动成功后，仍被服务端检测不通过
        List<WebElement> slider = webDriver.findElements(By.id("yodaMoveingBar"));
        if (!slider.isEmpty()) {
            new Actions(webDriver).clickAndHold(slider.get(0)).perform();
            String width = webDriver.findElement(By.id("yodaBoxWrapper")).getCssValue("width");
            Point target = new Point(Integer.parseInt(width.substring(0, width.length() - 2)), 1);
            // TODO 轨迹优化
            int[][] tracks = AntiCaptcha.generateTrack(target, 50, 500, target, 1.0d, 1.0d, 0, 0, 0, 0, 0, 0);
            AntiCaptcha.slideFollowTrack(webDriver, tracks, 500);
            new Actions(webDriver).release(slider.get(0)).perform();
            List<WebElement> statusElement = webDriver.findElements(By.id("yodaBox"));
            if (!statusElement.isEmpty() && "boxOk".equals(statusElement.get(0).getAttribute("class"))) { // class="boxStatic" --> class="boxOk"
                return true;
            }
        }
        log.error("unknown captcha");
        return false;
    }

    private static byte[] parseImgFromElement(WebElement element) {
        String img = element.getCssValue(BG_IMG_CSS_PROPERTY);
        if (!img.startsWith(BG_IMG_DATA_PREFIX)) {
            throw new ForceStopException("slide of dom change");
        }
        return Base64.getDecoder().decode(img.substring(BG_IMG_DATA_PREFIX.length(), img.length() - BG_IMG_DATA_SUFFIX.length()));
    }

    private static boolean is(WebDriver webDriver, String expect, String not) {
        if (expect != null) {
            return normalizeUrl(webDriver.getCurrentUrl(), expect).equals(webDriver.getCurrentUrl());
        }
        return !normalizeUrl(webDriver.getCurrentUrl(), not).equals(webDriver.getCurrentUrl());
    }

    // 跳转登陆界面
    public static void handleLogin(WebDriver webDriver, String expect) {
        log.warn("current page is {}", webDriver.getCurrentUrl());
        throw new ForceStopException("login page, exit now");
    }
}
