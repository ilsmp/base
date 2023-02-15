package com.ilsmp.base.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.ilsmp.base.BaseController;
import com.ilsmp.base.util.JsonUtil;
import com.ilsmp.base.util.Response;
import com.ilsmp.base.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一处理异常
 */
@Slf4j
@ControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handle(HttpServletRequest request, HttpServletResponse response,
                                                   Exception ex) {
        log.error(String.format("exception-request-id<%s>", response.getHeader("request-id")), ex);
        String message = getExceptionMessage(ex);
        if (StringUtil.isEmpty(message)) {
            message = "请求异常请查看日志或联系管理员";
        }
        if (ex instanceof HttpStatusCodeException) {
            return Response.withBody(message, ((HttpStatusCodeException) ex).getStatusCode());
        } else {
            return Response.withBody(message, HttpStatus.valueOf(400));
        }
    }

    @RestControllerAdvice(assignableTypes = BaseController.class)
    public static class BaseExceptionHandler implements ResponseBodyAdvice<Object> {
        @Override
        public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
            return true;
        }

        @SneakyThrows
        @Override
        public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                      MediaType selectedContentType,
                                      Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                      ServerHttpRequest request,
                                      ServerHttpResponse response) {
            if (body instanceof String) {
                return JsonUtil.writeJsonStr(Response.withResult(body));
            } else if (body instanceof Response){
                return body;
            } else{
                return Response.withResult(body);
            }
        }
    }

    public static String getExceptionMessage(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        t.printStackTrace(writer);
        String log = sw.getBuffer().toString();
        log = log.substring(0, log.indexOf("\n"));
        return log.substring(log.lastIndexOf(";")+1).trim();
    }

}
