package robot.crawler.reactor;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class HttpSupport {

    public static final String COOKIE_PAIR_SEPARATOR = ";";

    public static final String KEY_VALUE_SEPARATOR = "=";

    public static final String QUERY_STRING_JOINER = "&";

    public static void handleCookies(String cookies, BiConsumer<String, String> cookieConsumer) {
        if (cookies == null) {
            throw new IllegalArgumentException("cookie cannot be null");
        }
        String[] cookiePairs = cookies.split(COOKIE_PAIR_SEPARATOR);
        for (String pair : cookiePairs) {
            String[] cookiePair = pair.split(KEY_VALUE_SEPARATOR);
            cookieConsumer.accept(cookiePair[0].trim(), cookiePair[1]);
        }
    }

    /**
     * 将查询解析为map，注意不支持参数名有多个值（a=1&a=2）
     * @param queryString
     */
    public static void parseQueryString(String queryString, Map<String, String> map) {
        if (queryString == null) {
            return;
        }
        String[] kv = queryString.split(QUERY_STRING_JOINER);
        for (String pair : kv) {
            String[] param = pair.split(KEY_VALUE_SEPARATOR);
            map.put(param[0], param[1]);
        }
    }
}
