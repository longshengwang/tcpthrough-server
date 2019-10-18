package org.wls.tcpthrough.model;

/**
 * Created by wls on 2019/10/16.
 */
public enum ResponseType {
    REGISTER_RESPONSE(1), NEW_CONN_RESPONSE(2), NEW_CONF_RESPONSE(3),DELETE_CONF_RESPONSE(4),REGISTER_FAIL(5);

    // 定义一个 private 修饰的实例变量
    private int type;

    // 定义一个带参数的构造器，枚举类的构造器只能使用 private 修饰
    private ResponseType(int type) {
        this.type = type;
    }

    // 定义 get set 方法
    public int get() {
        return type;
    }
}

