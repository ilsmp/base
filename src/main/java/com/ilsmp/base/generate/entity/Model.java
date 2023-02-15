package com.ilsmp.base.generate.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;

/**
 * 返回属性
 */
@Data
public class Model implements Serializable {
    /**
     * 类名
     */
    private String className;
    /**
     * 属性名
     */
    private String name;
    /**
     * header/param/body
     */
    private String in;
    /**
     * 类型
     */
    private String type;
    private HashMap<String, String> schema;
    /**
     * 是否必填
     */
    private Boolean require = false;
    /**
     * 属性描述
     */
    private String description;
    /**
     * 嵌套属性列表
     */
    private List<Model> properties = new ArrayList<>();
    /**
     * 是否加载完成，避免循环引用
     */
    private boolean isCompleted = false;
}
