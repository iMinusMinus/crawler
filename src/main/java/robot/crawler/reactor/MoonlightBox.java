package robot.crawler.reactor;

import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * 月光宝盒
 */
public class MoonlightBox extends Element implements WebElement {

    private final WebElement operationDelegate;

    private final Element dom;

    public MoonlightBox(String tagName) {
        super(tagName);
        operationDelegate = null;
        dom = null;
    }

    public MoonlightBox(WebElement operationDelegate, Element dom) {
        super(dom.tagName());
        this.dom = dom;
        this.operationDelegate = operationDelegate;

    }

    WebElement getOperationDelegate() {
        return operationDelegate;
    }

    @Override
    public void click() {
        operationDelegate.click();
    }

    @Override
    public void submit() {
        operationDelegate.submit();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        operationDelegate.sendKeys(keysToSend);
    }

    @Override
    public void clear() {
        operationDelegate.clear();
    }

    @Override
    public String getTagName() {
        return tagName();
    }

    @Override
    public String getAttribute(String name) {
        return dom.attributes().get(name);
    }

    @Override
    public boolean isSelected() {
        return operationDelegate.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return operationDelegate.isEnabled();
    }

    @Override
    public String getText() {
        return dom.text();
    }

    @Override
    public List<WebElement> findElements(By by) {
        List<WebElement> elements = by.findElements(operationDelegate);
        List<Element> domElements;
        if (by instanceof By.ByXPath byXpath) {
            domElements = dom.selectXpath((String) byXpath.getRemoteParameters().value());
        } else if (by instanceof By.ByCssSelector cssSelector) {
            domElements = dom.select((String) cssSelector.getRemoteParameters().value());
        } else {
            throw new RuntimeException("unsupported selector");
        }
        assert  elements.size() == domElements.size();
        List<WebElement> container = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            container.add(new MoonlightBox(elements.get(i), domElements.get(i)));
        }
        return container;
    }

    @Override
    public WebElement findElement(By by) {
        Element domElement;
        if (by instanceof By.ByXPath byXpath) {
            domElement = dom.selectXpath((String) byXpath.getRemoteParameters().value()).get(0);
        } else if (by instanceof By.ByCssSelector cssSelector) {
            domElement = dom.selectFirst((String) cssSelector.getRemoteParameters().value());
        } else {
            throw new RuntimeException("unsupported selector");
        }
        return new MoonlightBox(by.findElement(operationDelegate), domElement);
    }

    @Override
    public boolean isDisplayed() {
        return operationDelegate.isDisplayed();
    }

    @Override
    public Point getLocation() {
        return operationDelegate.getLocation();
    }

    @Override
    public Dimension getSize() {
        return operationDelegate.getSize();
    }

    @Override
    public Rectangle getRect() {
        return operationDelegate.getRect();
    }

    @Override
    public String getCssValue(String propertyName) {
        return operationDelegate.getCssValue(propertyName);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        return operationDelegate.getScreenshotAs(target);
    }
}
