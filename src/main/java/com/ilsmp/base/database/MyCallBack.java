package com.ilsmp.base.database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ZJH Title: MyCallBack Package com.zhihui.gongdi.tool Description: 回调接口 Date 2020/3/25 11:57
 */
public interface MyCallBack<T> extends Serializable {

    public static String FIRST = "first";
    public static String SECOND = "second";
    public static String FIRST_PREFIX = "spring.datasource";
    public static String SECOND_PREFIX = "spring.datasource2";

    /**
     * 回调方法
     */
    default <T> void callBack(T t) {
    }

    /**
     * 回调方法
     */
    public default Map<String, Object> callNum() {
        return new HashMap<>();
    }
}
