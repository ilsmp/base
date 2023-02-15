package com.ilsmp.base;

import java.io.IOException;
import java.io.Serializable;

import com.ilsmp.base.util.ServletUtil;

/**
 * @author: ZJH Title: BaseService Package com.zhihui.gongdi.tool Description: 基础Service类 Date 2020/3/19 18:11
 */
public class BaseService implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String getRequestHeaderDecode(String headerName) {
        return ServletUtil.getRequestHeaderDecode(headerName);
    }

    protected String getRequestHeader(String headerName) {
        return ServletUtil.getRequestHeader(headerName);
    }

    protected Object getRequestBodyDecode(String bodyName) throws IOException {
        return ServletUtil.getRequestBodyDecode(bodyName);
    }

    protected Object getRequestBody(String bodyName) throws IOException {
        return ServletUtil.getRequestBody(bodyName);
    }

    protected String getRequestParamDecode(String paramName) {
        return ServletUtil.getRequestParamDecode(paramName);
    }

    protected String getRequestParam(String paramName) {
        return ServletUtil.getRequestParam(paramName);
    }

    protected Object getRequestObject(String name) throws IOException {
        return ServletUtil.getRequestObject(name);
    }

    protected Object getRequestObjectDecode(String name) throws IOException {
        return ServletUtil.getRequestObjectDecode(name);
    }

}
