package org.wls.tcpthrough.http.lib.entity;

/**
 * Created by shirukai on 2018/9/30
 * 参数信息实体类
 */
public class Param {
    private String type;
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
