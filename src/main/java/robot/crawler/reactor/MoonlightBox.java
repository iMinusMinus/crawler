package robot.crawler.reactor;

import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * 月光宝盒
 */
public class MoonlightBox extends Element implements WebElement, WrapsElement {

    private final Element dom;

    @SuppressWarnings({"unused"})
    public MoonlightBox(String tagName) {
        super(tagName);
        dom = null;
    }

    public MoonlightBox(Element dom) {
        super(dom.tagName());
        this.dom = dom;

    }

    // for actions retrieve origin WebElement
    @Override
    public WebElement getWrappedElement() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public void click() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public void submit() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("I'm not real WebElement");
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
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public String getText() {
        return dom.text();
    }

    @Override
    public List<WebElement> findElements(By by) {
        List<Element> domElements;
        if (by instanceof By.ByXPath byXpath) {
            domElements = dom.selectXpath((String) byXpath.getRemoteParameters().value());
        } else if (by instanceof By.ByCssSelector cssSelector) {
            domElements = dom.select((String) cssSelector.getRemoteParameters().value());
        } else {
            throw new RuntimeException("unsupported selector");
        }
        List<WebElement> container = new ArrayList<>();
        for (Element domElement : domElements) {
            container.add(new MoonlightBox(domElement));
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
        return new MoonlightBox(domElement);
    }

    @Override
    public boolean isDisplayed() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public Point getLocation() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public Dimension getSize() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public Rectangle getRect() {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public String getCssValue(String propertyName) {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        throw new UnsupportedOperationException("I'm not real WebElement");
    }



    /* for selector retrieve dom elements */

    @Override
    public Elements selectXpath(String xpath) {
        W3CDom w3c = (new W3CDom()).namespaceAware(false);
        org.w3c.dom.Document wDoc = w3c.fromJsoup(dom);
        org.w3c.dom.Node contextNode = w3c.contextNode(wDoc);
        NodeList nodeList = w3c.selectXpath(xpath, contextNode);
        return new Elements(w3c.sourceNodes(nodeList, Element.class));
    }

    // cssQuery must compatible with browser, sample: #id1 > tag.class1.class2 > tag1[attrKey='attrValue'] > tag2:pseudo-class + tag2
    @Override
    public Elements select(String cssQuery) {
        return dom.select(cssQuery);
    }
}
