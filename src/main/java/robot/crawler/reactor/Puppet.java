package robot.crawler.reactor;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Set;

public record Puppet(Connection conn, Document doc) implements WebDriver {

    @Override
    public void get(String url) {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public String getCurrentUrl() {
        return doc.baseUri();
    }

    @Override
    public String getTitle() {
        return doc.title();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return new MoonlightBox(doc).findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return new MoonlightBox(doc).findElement(by);
    }

    @Override
    public String getPageSource() {
        return doc.html();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public void quit() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public Set<String> getWindowHandles() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public String getWindowHandle() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public TargetLocator switchTo() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public Navigation navigate() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }

    @Override
    public Options manage() {
        throw new UnsupportedOperationException("I'm not real WebDriver");
    }
}
