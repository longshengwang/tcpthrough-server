package org.wls.tcpthrough.http.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shirukai on 2018/10/8
 * 处理URL中的参数
 */
public class PathUrlHandler {
    private static final String pattern = "(\\{[^}]*})";


    /**
     * 校验请求url和路由中的url是否匹配
     * 如：/test/111 匹配 /test/{id}
     *
     * @param requestUrl 请求的url
     * @param routerUrl  路由中设置的url
     * @return boolean
     */
    public static boolean verify(String requestUrl, String routerUrl) {
        Matcher keyMatcher = Pattern.compile(pattern).matcher(routerUrl);
        String replacePattern = keyMatcher.replaceAll("(.*)");
        Matcher valueMatcher = Pattern.compile(replacePattern).matcher(requestUrl);
        return valueMatcher.matches();
    }

    /**
     * 获取参数
     * 请求URL：/test/111
     * 路由URL：/test/{id}
     * 参数为{"id":"111"}
     *
     * @param requestUrl 请求的url
     * @param routerUrl  路由中设置的url
     * @return map
     */
    public static Map<String, String> getParams(String requestUrl, String routerUrl) {
        Map<String, String> params = new HashMap<>(16);
        Matcher keyMatcher = Pattern.compile(pattern).matcher(routerUrl);
        List<String> keys = new ArrayList<>(16);
        List<String> values = new ArrayList<>(16);
        while (keyMatcher.find()) {
            keys.add(keyMatcher.group(1).replace("{", "").replace("}", ""));
        }
        String replacePattern = keyMatcher.replaceAll("(.*)");
        Matcher valueMatcher = Pattern.compile(replacePattern).matcher(requestUrl);
        if (valueMatcher.find()) {
            int count = valueMatcher.groupCount();
            for (int i = 1; i <= count; i++) {
                values.add(valueMatcher.group(i));
            }
        }
        int valueSize = values.size();
        for (int i = 0; i < keys.size(); i++) {
            String value = i < valueSize ? values.get(i) : "";
            params.put(keys.get(i), value);
        }
        return params;
    }

}
