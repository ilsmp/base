package com.ilsmp.base.util;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

/**
 * Author: ZJH Title: Amend Package com.zhihui.gongdi.controller Description: 整改记录表 Date 2019/10/8 16:08
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
@Setter
@Getter
@Schema(name = "response请求响应结构类",title = "请求响应结构类", description = "请求响应结构类")
@Tag(name = "response请求响应结构类", description = "请求响应结构类")
public class Response<T> implements Serializable {
    /**
     * serialVersionUID
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应状态码", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false,
            type = "Integer", format = "int", example = "0", defaultValue = "0")
    private int code;
    @Schema(description = "响应状态说明",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true, type = "string", format = "varchar(1000)",
            example = "123", defaultValue = "123")
    private String message;
    @Schema(description = "响应数据",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true, type = "T", format = "t",
            example = "123", defaultValue = "123")
    private T result;

    public Response() {
    }

    public Response(int code, String message, @Nullable T result) {
        this.code = code;
        this.message = message;
        this.result = result;
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