package robot.crawler.reactor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import robot.crawler.anti.AntiDianPingAntiCrawler;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AntiDianPingAntiCrawlerTest {

    @Test
    public void testToken() {
        String pageParam = "cityId=1&order=undefined&shopId=l6cuO8nXLn75Dd4N&shopType=10&summaryName=undefined&tcv=qo767wnn8n";
        Map<String, Object> analyticParams = new LinkedHashMap<>();
        // mobile: 100046, account: 100047, mobile_wx/mobile_qq: 100050
        analyticParams.put("rId", 100041); // Rohr_Opt.Flag <--
        analyticParams.put("ver", "1.0.6"); // ${constant}[${index}] <-- 1.0.6
        analyticParams.put("ts", 1683019047955L); // new Date().getTime() <-- System.currentTimeMillis()
        analyticParams.put("cts", 1683019050816L); // new Date().getTime() <-- System.currentTimeMillis()
        analyticParams.put("brVD", new int[]{908,1289}); // function() {return [Math.max(document.documentElement.clientWidth, window.innerWidth || 0), Math.max(document.documentElement.clientHeight, window.innerHeight || 0)} <-- [screen.availWidth, screen.availHeight]
        Object[] brR = new Object[4];
        brR[0] = new int[] {2560,1440};
        brR[1] = new int[] {2560,1392};
        brR[2] = 24;
        brR[3] = 24;
        analyticParams.put("brR", brR); // function() {return [[screen.width, screen.height], [screen.availWidth, screen.availHeight], screen.colorDepth, screen.pixelDepth];}
        analyticParams.put("bI", new String[] {"https://www.dianping.com/shop/l6cuO8nXLn75Dd4N","https://www.dianping.com/search/keyword/1/0_%E9%98%B3%E6%98%A5%E9%9D%A2%20%E6%8E%A7%E6%B1%9F%E8%B7%AF%E5%BA%97"}); // function() {return [document.referrer, window.location.href];}
        analyticParams.put("mT", Collections.emptyList()); // [] <-- Collections.emptyList()
        analyticParams.put("kT", Collections.emptyList()); // [] <-- Collections.emptyList()
        analyticParams.put("aT", Collections.emptyList()); // [] <-- Collections.emptyList()
        analyticParams.put("tT", Collections.emptyList()); // [] <-- Collections.emptyList()
        // 检测自动化：__lastWatirAlert, __lastWatirConfirm, __lastWatirPrompt --> wwt
        // 检测PhantomJS(_phantom, phantom, callPhantom) --> ps
        // 检测WebDriver(document.webdriver) --> dw, (document.__webdriver_evaluate, document.__webdriver_unwrapped) --> de, __webdriverFunc --> wf, webdriver --> ww, navigator.webdriver --> gw
        // 检测Selenium(document.__selenium_evaluate, document.__selenium_unwrapped) --> de, (document._Selenium_IDE_Recorder, document._selenium, document.calledSelenium) --> di
        // 检测FxDriver(document.__fxdriver_evaluate, document.__fxdriver_unwrapped) --> de
        // (domAutomation, domAutomationController) -> ""
        analyticParams.put("aM", ""); // function() {} <-- ""

        Assertions.assertEquals("eJx1kG1vgjAUhf9Lk36SQAsUKIkfQDBTUaYVnRqzKPjCEFTAF1z231cWl+zLkpucp+eem5z0E+SdCJgYIaRiAVw3OTABFpGoAQGUBd9ohoIwRapOCRFA+NcjyMCaANb5xAHmgiJDwLJBl7Uz4sZCJhoSsKqipfBkhcpLQVb51KkOD4F9WZ4KU5Jut5sYxavsFGc7MTymUrE/nqSDFl58I3vzMp04kTrgrf4/2KzycC8lm+p2zCMJS+gduhRSA9oKdLUaLPLjONCSoYxq03ChpddgY0jb0OVhHVocCLQtSHXAi6ZjXpRr8tTVU8vfd59/Gm9WxLuM06Z7H7NCLc7bUb8YT4Kqoj3G5MoLsccCxXu45SBgV79qGRa7sFl3vtWmjVG74bN5qp+T9DW270H/3MLd0hkqQZyUj/Due70rYiH2R1F3lhxoMiWnyzqdbzMyyZLe0LVkEny8WM0m+PoGdK2FbA==",
                AntiDianPingAntiCrawler.token(analyticParams, pageParam));
    }
}
