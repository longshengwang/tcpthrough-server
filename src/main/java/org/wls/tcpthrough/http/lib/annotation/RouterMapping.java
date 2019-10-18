package org.wls.tcpthrough.http.lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by shirukai on 2018/9/30
 * 路由
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RouterMapping {
    String api() default "";

    String method() default "GET,POST,PUT,DELETE";
}
