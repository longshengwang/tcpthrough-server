package org.wls.tcpthrough.http.lib.entity;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by shirukai on 2018/9/30
 * 路由信息实体类
 */
public class Router {
    private String url;

    public Class<?> gettClass() {
        return tClass;
    }

    public void settClass(Class<?> tClass) {
        this.tClass = tClass;
    }

    private Class<?> tClass;
    private Method method;
    private String methodType;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    private List<Param> params;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }
}
