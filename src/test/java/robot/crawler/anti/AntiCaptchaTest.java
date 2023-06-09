package robot.crawler.anti;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiCaptchaTest {

    @Test
    public void testEDun() throws Exception {
        // export msedgedriver.exe to webdriver.edge.driver
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        // export opencv_java* to java.library.path
        WebDriver webDriver = new EdgeDriver();
        String startUrl = "https://dun.163.com/trial/jigsaw";
        webDriver.get(startUrl);
        WebElement dragable = webDriver.findElement(By.cssSelector("body > main > div.g-bd > div > div.g-mn2 > div.m-tcapt > div.tcapt-type__container > div.tcapt-type__item.active > div > div > div.tcapt_content > div.u-fitem.u-fitem-capt > div > div > div.yidun_control > div.yidun_slider.yidun_slider--hover"));
        WebElement templImage = webDriver.findElement(By.cssSelector("body > main > div.g-bd > div > div.g-mn2 > div.m-tcapt > div.tcapt-type__container > div.tcapt-type__item.active > div > div > div.tcapt_content > div.u-fitem.u-fitem-capt > div > div > div.yidun_panel > div > div.yidun_bgimg > img.yidun_jigsaw"));
        WebElement backgroundImage = webDriver.findElement(By.cssSelector("body > main > div.g-bd > div > div.g-mn2 > div.m-tcapt > div.tcapt-type__container > div.tcapt-type__item.active > div > div > div.tcapt_content > div.u-fitem.u-fitem-capt > div > div > div.yidun_panel > div > div.yidun_bgimg > img.yidun_bg-img"));
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        byte[]  jigsaw = httpClient.send(HttpRequest.newBuilder().GET().uri(URI.create(templImage.getAttribute("src"))).build(), HttpResponse.BodyHandlers.ofByteArray()).body();
        byte[] bg = httpClient.send(HttpRequest.newBuilder().GET().uri(URI.create(backgroundImage.getAttribute("src"))).build(), HttpResponse.BodyHandlers.ofByteArray()).body();
        new Actions(webDriver).clickAndHold(dragable).perform();
        AntiCaptcha.passThroughSlideCaptcha(webDriver, jigsaw, bg, 1, 10);
        new Actions(webDriver).release(dragable).perform();
        List<WebElement> elementList = webDriver.findElements(By.cssSelector("body > main > div.g-bd > div > div.g-mn2 > div.m-tcapt > div.tcapt-type__container > div.tcapt-type__item.active > div > div > div.tcapt_content > div.u-fitem.u-fitem-capt > div > div > div.yidun_control.yidun_control--moving > div.yidun_slider.yidun_slider--hover > span"));
        Assertions.assertTrue(elementList.size() > 0 || !startUrl.equals(webDriver.getCurrentUrl()));
        webDriver.quit();
    }

    @ParameterizedTest
    @ValueSource(strings = {"103a92913a6544a1b25ed806081476ab"})
    @Disabled
    public void testDianping(String requestCode) throws Exception {
        WebDriver webDriver = new ChromeDriver();
        String startUrl = "https://verify.meituan.com/v2/web/general_page?action=spiderindefence&requestCode=" + requestCode + "&platform=1000&adaptor=auto&succCallbackUrl=https%3A%2F%2Foptimus-mtsi.meituan.com%2Foptimus%2FverifyResult%3ForiginUrl%3Dhttps%253A%252F%252Fwww.dianping.com%252F&theme=dianping";
        webDriver.get(startUrl);
        WebElement sliderDrag = webDriver.findElement(By.id("puzzleSliderDrag")); // 滑动匹配图
        WebElement main = webDriver.findElement(By.id("puzzleImageMain")); // 背景图
        WebElement draggable = webDriver.findElement(By.id("puzzleSliderBox")); // puzzleSliderMoveingBar, 拖动条
        new Actions(webDriver).clickAndHold(draggable).perform();
        String slideBase64 = sliderDrag.getCssValue("background-image");
        byte[] slidePng = Base64.getDecoder().decode(slideBase64.substring(27, slideBase64.length() - 2));
        String backgroundBase64 = main.getCssValue("background-image"); //
        byte[] backgroundPng = Base64.getDecoder().decode(backgroundBase64.substring(27, backgroundBase64.length() - 2));
        AntiCaptcha.setDebug(true);
        Point offset = AntiCaptcha.edgeOffset(slidePng);
        AntiCaptcha.passThroughSlideCaptcha(webDriver, slidePng, backgroundPng, 1d/3d, -offset.x);
        new Actions(webDriver).release(draggable).perform();
        webDriver.quit();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2", "3", "4", "5"})
    public void testDianpingLocal(String no) {
        Map<String, Integer> maybe = new HashMap<>();
        maybe.put("", 608); // x: 743~836, y: 314~407
        maybe.put("2", 500); // x: 637~730, y: 474~567
        maybe.put("3", 641);// x: 778~872, y: 367~461
        maybe.put("4", 430);// x: 567~660, y: 46~139
        maybe.put("5", 500);// x: 637~730, y: 421~514
        try (FileInputStream fis1 = new FileInputStream("src/test/resources/dianping-puzzleImageMain%1$S.txt".formatted(no));
             FileInputStream fis2 = new FileInputStream("src/test/resources/dianping-puzzleSliderDrag%1$S.txt".formatted(no))) {
            String bgBase64 = new String(fis1.readAllBytes());
            byte[] bs = Base64.getDecoder().decode(bgBase64.substring(27, bgBase64.length() - 2));
            String slideBase64 = new String(fis2.readAllBytes());
            byte[] ss = Base64.getDecoder().decode(slideBase64.substring(27, slideBase64.length() - 2));
            AntiCaptcha.setDebug(true);
            Point offset = AntiCaptcha.edgeOffset(ss);
            System.out.println(offset);
            Point result = AntiCaptcha.calculateDistance(ss, bs, -offset.x, -offset.y);
            System.out.println(result);
            Assertions.assertTrue(Math.abs(result.x - maybe.get(no)) < 5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
