package org.wls.tcpthrough.http.lib;




import org.wls.tcpthrough.http.lib.annotation.JsonParam;
import org.wls.tcpthrough.http.lib.annotation.PathParam;
import org.wls.tcpthrough.http.lib.annotation.RequestParam;
import org.wls.tcpthrough.http.lib.annotation.RouterMapping;
import org.wls.tcpthrough.http.lib.entity.Param;
import org.wls.tcpthrough.http.lib.entity.Router;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shirukai on 2018/9/30
 * 注解扫描
 */
public class AnnotationScan {
    public static Map<String, Router> getRouters(Class<?>... classes) {
        Map<String, Router> routers = new HashMap<>(16);
        // 遍历传入的class
        for (Class<?> tClass : classes) {
            // 遍历类中的方法
            for (Method method : tClass.getDeclaredMethods()) {
                // 扫描方法的注解
                RouterMapping routerMapping = method.getAnnotation(RouterMapping.class);
                // 如果注解不为空，扫描该方法内的参数注解
                if (routerMapping != null) {
                    Router router = new Router();
                    // 获取参数
                    List<Param> params = new ArrayList<>(16);
                    // 遍历方法中的参数
                    for (Parameter parameter : method.getParameters()) {
                        // 扫描参数注解
                        PathParam pathParam = parameter.getAnnotation(PathParam.class);
                        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                        JsonParam jsonParam = parameter.getAnnotation(JsonParam.class);
                        Param param = new Param();
                        // 将注解写入到信息写入到Param中
                        if (pathParam != null) {
                            param.setType("path");
                            param.setValue(pathParam.value());
                        }
                        if (requestParam != null) {
                            param.setType("request");
                            param.setValue(requestParam.value());
                        }
                        if (jsonParam != null) {
                            param.setType("json");
                            param.setValue(jsonParam.value());
                        }
                        // 将Param信息写入到List里
                        params.add(param);
                    }
                    router.setParams(params);
                    router.settClass(tClass);
                    router.setMethodType(routerMapping.method());
                    router.setMethod(method);
                    router.setUrl(routerMapping.api());
                    // 保存Router信息
                    routers.put(router.getUrl(), router);
                }
            }
        }
        return routers;
    }
}
