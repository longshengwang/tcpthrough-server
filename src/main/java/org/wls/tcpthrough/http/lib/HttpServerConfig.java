package org.wls.tcpthrough.http.lib;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shirukai on 2018/9/30
 * 用来配置HttpServer
 */
public class HttpServerConfig {
    // 初始化Map用来缓存配置信息
    private ConcurrentHashMap<String, Object> conf = new ConcurrentHashMap<>();
    private static final String SERVER_CONTROLLERS = "netty.http.server.controllers";
    private static final String SERVER_PORT = "netty.http.server.port";
    private HttpServer httpServer;

    public HttpServerConfig(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public HttpServerConfig set(String name, Object value) {
        conf.put(name, value);
        return this;
    }

    /**
     * 设置端口号
     *
     * @param port 端口
     * @return conf
     */
    public HttpServerConfig setPort(int port) {
        conf.put(SERVER_PORT, port);
        return this;
    }

    /**
     * 设置多个Controller的class 以便进行注解扫描
     *
     * @param className class names
     * @return conf
     */
    public HttpServerConfig setControllers(Class<?>... className) {
        Class<?>[] oldClasses = getClasses();
        Class<?>[] newClasses = insertClasses(oldClasses, className);
        conf.put(SERVER_CONTROLLERS, newClasses);
        return this;
    }

    /**
     * 设置单个controller的class
     *
     * @param className class name
     * @return conf
     */
    public HttpServerConfig setController(Class<?> className) {
        //获取已有的class
        Class<?>[] oldClasses = getClasses();
        //新增class
        Class<?>[] newClasses = insertClass(oldClasses, className);
        conf.put(SERVER_CONTROLLERS, newClasses);
        return this;
    }

    /**
     * 获取String类型的值
     *
     * @param key key
     * @return value
     */
    public String getString(String key) {
        return conf.get(key).toString();
    }

    /**
     * 获取 int类型的值
     *
     * @param key key
     * @return value
     */
    public int getInt(String key) {
        return (int) conf.get(key);
    }

    /**
     * 获取所有的class
     *
     * @return classes
     */
    public Class<?>[] getClasses() {
        return (Class<?>[]) conf.get(SERVER_CONTROLLERS);
    }

    /**
     * 插入一个class
     *
     * @param oldClasses 旧的classes
     * @param addClass   要添加的class
     * @return 新的 classes
     */
    private Class<?>[] insertClass(Class<?>[] oldClasses, Class<?> addClass) {
        Class<?>[] newClasses;
        //判断原始的Class 数组是否为null
        if (oldClasses != null) {
            //获取数组长度
            int length = oldClasses.length;
            //创建一个新数组
            newClasses = new Class<?>[length + 1];
            //copy数组
            System.arraycopy(oldClasses, 0, newClasses, 0, length);
            newClasses[length + 1] = addClass;
        } else {
            newClasses = new Class<?>[]{addClass};
        }
        return newClasses;
    }

    /**
     * 插入多个class
     *
     * @param oldClasses 旧的classes
     * @param addClasses 要添加的classes
     * @return 新的classes
     */
    private Class<?>[] insertClasses(Class<?>[] oldClasses, Class<?>[] addClasses) {
        Class<?>[] newClasses;
        if (oldClasses != null) {
            int oldLength = oldClasses.length;
            int addLength = addClasses.length;
            newClasses = new Class<?>[oldLength + addLength];
            System.arraycopy(oldClasses, 0, newClasses, 0, oldLength);
            System.arraycopy(addClasses, 0, newClasses, oldLength, addLength);
        } else {
            newClasses = addClasses;
        }
        return newClasses;
    }

    public HttpServer create() {
        httpServer.setPort(this.getInt(SERVER_PORT));
        //获取controller类
        Class<?>[] classes = this.getClasses();
        //扫描注解
        httpServer.setRouters(AnnotationScan.getRouters(classes));
        return this.httpServer;
    }

    public ConcurrentHashMap<String, Object> getConf() {
        return this.conf;
    }

}
