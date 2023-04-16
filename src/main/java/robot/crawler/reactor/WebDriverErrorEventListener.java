package robot.crawler.reactor;

import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class WebDriverErrorEventListener implements WebDriverListener {

    private static final Logger log = LoggerFactory.getLogger(WebDriverErrorEventListener.class);

    @Override
    public void onError(Object target, Method method, Object[] args, InvocationTargetException e) {
        log.warn("error occur when invoke {}#{} with args: {}", target.getClass(), method.getName(), args);
    }
}
