package robot.crawler.reactor;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public record WebDriverWindowsEventListener(WebDriverContext context) implements WebDriverListener {

    private static Logger log = LoggerFactory.getLogger(WebDriverWindowsEventListener.class);

    private static final Set<String> openedWindows = new HashSet<>();

    private static final String START_PAGE_URL = "data:,";

    @Override
    public void beforeGet(WebDriver driver, String url) {
        if (START_PAGE_URL.equals(driver.getCurrentUrl())) { // webdriver启动页

        } else { // 修改地址栏请求，打开新窗口/tab请求
//            openedWindows.add(driver.getWindowHandle());
//            openedWindows.addAll(driver.getWindowHandles());
        }
    }

    @Override
    public void afterGet(WebDriver driver, String url) {
        Set<String> latest = driver.getWindowHandles();
        for (String opened : latest) {
            if (openedWindows.add(opened)) {
                log.info("automatic switch window to [{}]", url);
                driver.switchTo().window(opened);
                context.activeWindow(opened);
            }
        }
    }

    // FIXME
    // click/submit may open new windows/tab, but we cannot handle it by WebDriverListener
    // as click/perform/anyWebDriverCall either missing WebDriver parameter or WebElement

    @Override
    public void beforeClose(WebDriver driver) {
        String closed = context.destroyWindow();
        openedWindows.remove(closed);
    }

    @Override
    public void afterClose(WebDriver driver) {
        // switch window after close or driver.getWindowHandle throws Exception
        // org.openqa.selenium.NoSuchWindowException: no such window: target window already closed
        driver.switchTo().window(context.currentWindow());
    }

    @Override
    public void beforeQuit(WebDriver driver) {
        openedWindows.clear();
    }
}
