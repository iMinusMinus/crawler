package robot.crawler.anti;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// @see https://s3plus.sankuai.com/static-prod01/com.sankuai.dpaccountweb.maccount-files/4d1da98/js/pclogin/index.js
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

    @Test
    public void testLogin() {
        // /mlogin/dp/api/v1/account/passwordlogin/login <-- passwordLogin
        String url = "https://accountapi.dianping.com/mlogin/dp/api/v1/account/passwordlogin/login";
        Map<String, String> form = new HashMap<>();
        //             var n = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtABocAwAJuxcPN8tsrXwHA0kQrFezWwFwQDi6F1QYHVib4NBnQNuq712x0lxHrAbYc85tR8881W3y8DqcbpkGn82AYVXVi4eijFcJCnBO4tZRaPEtKFq6n4aXx0rOEumYsFUPXkSf5foS5zJl7RxZkRCadp1WkJfg51ZkiNoJ4Aav8pSUg+lrmf69nApsZXW3UCgOL1R0Lo2rh3w67QLJ+Z0KGH/H2tOJioBEMTON55VyePfXnk81zFhnNOnHXCMJl5VmhvJYf/Xp1GgxZJPCD4owgExia0dApzauqyFaJcQulBIvftJ+mAsU04sycfTrpjD0gSgXA2Iu1oKWRxHAQIDAQAB"
        //              , r = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRD8YahHualjGxPMzeIWnAqVGMIrWrrkr5L7gw+5XT55iIuYXZYLaUFMTOD9iSyfKlL9mvD3ReUX6Lieph3ajJAPPGEuSHwoj5PN1UiQXK3wzAPKcpwrrA2V4Agu1/RZsyIuzboXgcPexyUYxYUTJH48DeYBGJe2GrYtsmzuIu6QIDAQAB";
        // +15678901234 JSEncrypt o = new JSEncrypt(); o.setPublicKey(n); o.encrypt
        // bae64encode(encrypt())
        form.put("encryptMobile", "YoclQ8m6HkvD251HkwA+/O5HlKQ0/DZBe+ymceB4zMVmOZl3Op4pdzfBO+Z4auTlHVsluGJSPK7CN+ZyYO4s17CBUHevb50bsRZ3KqQTVpcWhxrJKVz2/dE8qlL/M1nzg9X01IY04b6DN12Loxb9dZPWMfT1g6xugYjcMUZaLlRpO02pa8h3Zvd7p5+qq3Awgz57Sj5jgTnPnLkgS7J6YRJceP7ji3tCfwgOtur2V8G/+0vuZ5dx2UuyNJqZhjwvdeuhYyIGxP6NO96GPHXS5JtVpGKEvKOOk09QcM/snSQqW3Ix9XrmZz2md1sD8eIAOfT2AJvDrNPOjXNMCgnlJQ==");
        form.put("countryCode", "86");
        // dianping
        form.put("encryptPassword", "Kjr26wqi8kU8pFYPcyRLfaA/3tZf4GS2KhjPjLr+En1tPEvIijaz2RT+Xb44BZW0aHP0NOQLU5Mi2xFc1xoeQM73JzKANXjkX39jANdpgnYOwvtdpyXgpx72rEhh99IwSY6HRxAMMeGItQA+lj7e0IGAG0HkXBvpFl/gFGIQurc=");
        form.put("uuid", "1875e4b8208c8-06b373e7920483-7a545474-384000-1875e4b8208c8");
        form.put("cx", "cxcxcx");
        form.put("setCookie", "true"); // <-- !0
        form.put("platform", "1"); // <-- 1
        form.put("siteType", "PC"); // <-- PC
        // <-- getFingerPrint <-- H5guard && H5guard.getfp ? H5guard.getfp() : "" <-- https://appsec-mobile.meituan.com/h5guard/H5guard.js?v=1683949532024
        form.put("h5_fingerprint", "H5dfp_1.8.2_tttt_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");
        // k0 ~ k62, k10 fn create span/div element with font etc. k60 fn create div element

    }
}
