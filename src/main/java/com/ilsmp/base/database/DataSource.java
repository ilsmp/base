package com.ilsmp.base.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Author: zhangjiahao04
 * Description: 动态数据源注解
 * Date: 2022/4/6 17:44
 **/
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    String value() default MyCallBack.FIRST;
}
