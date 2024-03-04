package com.ilsmp.base.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

/**
 * Author: ZJH Title: Amend Package com.zhihui.gongdi.controller Description: 整改记录表 Date 2019/10/8 16:08
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Schema(title = "response", description = "响应类")
public class Response<T> implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T result;

    public Response() {
    }

    public Response(int code, String message, @Nullable T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Response)) {
            return false;
        }
        Response<?> that = (Response<?>) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(result, that.result);
    }

    @Override
    public String toString() {
        return "Response{" +
                "code='" + code + '\'' +
                ", reason='" + message + '\'' +
                ", result=" + result +
                '}';
    }

    public static <T> ResponseEntity<Response<T>> withBody(final T body) {
        return withBody(body, HttpStatus.OK);
    }

    public static <T> ResponseEntity<Response<T>> withBody(@Nullable T body, HttpStatus status) {
        if (body == null) {
            return new ResponseEntity<>(new Response<>(200, "查询结果为null", null), HttpStatus.OK);
        } else if (body instanceof Collection) {
            if (((Collection<?>) body).size() == 0) {
                return new ResponseEntity<>(new Response<>(200, "查询到0条记录", body), HttpStatus.OK);
            }
        } else if (body instanceof Integer) {
            if ((Integer) body == 0) {
                return new ResponseEntity<>(new Response<>(200, "未发现此记录", body), HttpStatus.OK);
            }
        } else if (!status.equals(HttpStatus.OK)) {
            String[] mes = body.toString().split("Exception: ");
            return new ResponseEntity<>(new Response<>(status.value(),
                    mes[mes.length - 1], body), status);
        }
        return new ResponseEntity<>(new Response<>(status.value(), "成功", body), status);
    }

    public static <T> Response<T> withResult(final T body) {
        return withResult(body, HttpStatus.OK);
    }

    public static <T> Response<T> withResult(@Nullable T body, HttpStatus status) {
        if (body == null) {
            return new Response<>(200, "查询结果为null", null);
        } else if (body instanceof Collection) {
            if (((Collection<?>) body).size() == 0) {
                return new Response<>(200, "查询到0条记录", body);
            }
        } else if (body instanceof Integer) {
            if ((Integer) body == 0) {
                return new Response<>(200, "未发现此记录", body);
            }
        } else if (!status.equals(HttpStatus.OK)) {
            String[] mes = body.toString().split("Exception: ");
            return new Response<>(status.value(), mes[mes.length - 1], body);
        }
        return new Response<>(status.value(), "成功", body);
    }

    public static <T> ResponseEntity<Object> withStatus(HttpStatus status) {
        return new ResponseEntity<>(new Response<>(status.value(), "成功", null), status);
    }

    public static <T> ResponseEntity<Object> withOk() {
        return withStatus(HttpStatus.OK);
    }

}

