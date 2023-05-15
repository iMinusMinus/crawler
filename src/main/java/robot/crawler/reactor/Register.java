package robot.crawler.reactor;

import robot.crawler.anti.AntiDianPingAntiCrawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class Register {

    private static Map<String, Object> applicationObjects;

    private static Map<Class, Object> applicationTypedObjects;

    private static Map<String /* window id*/, Map<Class, Object>> windowObjects;

    public static final String EXECUTOR_WEBDRIVER = "webdriver";

    public static final String EXECUTOR_JSOUP = "jsoup";

    public static <T> T getApplicationScopeObject(String name, Class<T> klazz) {
        if (applicationObjects == null) {
            throw new IllegalStateException("please initialize first!");
        }
        if (klazz == null) {
            throw new IllegalArgumentException("type must not null");
        }
        if (name != null) {
            Object bean = applicationObjects.get(name);
            if (bean != null && klazz.isAssignableFrom(bean.getClass())) {
                return klazz.cast(bean);
            }
        }
        Object bean = applicationTypedObjects.get(klazz);
        return bean == null ? null : klazz.cast(bean);
    }

    public static void registerApplicationObject(String name, Object bean) {
        if (applicationObjects == null) {
            throw new IllegalStateException("please initialize first!");
        }
        if (bean == null) {
            throw new IllegalArgumentException("bean must not null");
        }
        if (name != null) {
            applicationObjects.put(name, bean);
        }
        applicationTypedObjects.put(bean.getClass(), bean);
    }

    public static void registerWindowObject(String window, Class klazz, Object bean) {
        windowObjects.computeIfAbsent(window, (type) -> new HashMap<>()).put(klazz, bean);
    }

    public static <T> T getWindowObject(String window, Class<T> klazz) {
        return Optional.ofNullable(windowObjects.get(window))
                .map(t -> t.get(klazz)).map(klazz::cast)
                .orElse(null);
    }

    public static <T> T registerIfGetWindowObjectNotExist(String window, Class<T> klazz, Supplier<T> supplier) {
        T obj = getWindowObject(window, klazz);
        if (obj == null) {
            obj = supplier.get();
            registerWindowObject(window, klazz, obj);
        }
        return obj;
    }

    public static void destroyAllWindow() {
        windowObjects.clear();
    }

    public static void destroyWindow(String windowId) {
        windowObjects.remove(windowId);
    }

    /**
     * 应用初始化
     */
    public static void initialize() {
        applicationObjects = new ConcurrentHashMap<>();
        applicationTypedObjects = new ConcurrentHashMap<>();
        windowObjects = new ConcurrentHashMap<>();
        registerApplicationObject(EXECUTOR_WEBDRIVER, new WebDriverTaskExecutor());
        registerApplicationObject(EXECUTOR_JSOUP, new JsoupTaskExecutor());

        WebDriverStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_AUTH_DOMAIN,
                AntiDianPingAntiCrawler::handleVerify);
        WebDriverStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_LOGIN_DOMAIN,
                AntiDianPingAntiCrawler::handleLogin);
        WebDriverStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_HOST,
                AntiDianPingAntiCrawler::handleForbidden);
        JsoupStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_HOST,
                AntiDianPingAntiCrawler::failIfBlock);
//        JsoupStepHandlerFactory.registerAnti(AntiDianPingAntiCrawler.DIANPING_LOGIN_DOMAIN,
//                AntiDianPingAntiCrawler::handleLogin);
    }

    /**
     * 应用销毁
     */
    public static void destroy() {
        applicationObjects = null;
        applicationTypedObjects = null;
        windowObjects = null;
    }

}
