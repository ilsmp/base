package com.ilsmp.base.generate.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * Created Request
 */
@Data
public class Request implements Serializable {
    /**
     * 参数名
     */
    private String name;

    /*
     * Author: zhangjiahao04
     * Description: 参数路径
     **/
    private String in;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 参数类型
     */
    private String paramType;
    private String style;

    /**
     * 是否必填
     */
    private Boolean require;

    /**
     * 说明
     */
    private String remark;
    private String description;

    /**
     * 复杂对象引用
     */
    private Model model;

    public String getRemark() {
        return remark == null ? description : remark;
    }

    public String getParamType() {
        return paramType == null ? style : paramType;
    }
}
