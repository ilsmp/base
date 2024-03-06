package com.ilsmp.base.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xiaoymin.knife4j.spring.extension.Knife4jJakartaOperationCustomizer;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Description: 忽略参数 Package: com.ilsmp.base.config Author: zhangjiahao04 Title: PageIgnoreOperation Date:
 * 2024/3/3 19:27
 */

@Component
public class PageIgnoreOperation extends Knife4jJakartaOperationCustomizer {

    public static final String IGNORE_PARAMETER_EXTENSION_NAME = "x-ignoreParameters";

    private static final String[] IGNORE = new String[]{"numberOfElements", "pageable", "empty", "totalPages"};

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        setGlobalResponse(operation);
        if (hasPage(handlerMethod)) {
//            addExtensionParameters(IGNORE, operation);
//            addExtensionParameters(IGNORE, IGNORE_PARAMETER_EXTENSION_NAME, operation);
        }
        super.customize(operation, handlerMethod);
        return operation;
    }

    private boolean hasPage(HandlerMethod handlerMethod) {
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            Class<?> parameterType = parameter.getParameterType();
            if (parameterType.isAssignableFrom(Pageable.class)) {
                return true;
            }
        }
        return false;
    }

    private void setGlobalResponse(Operation operation) {
        ApiResponses responses = operation.getResponses();
        ApiResponse res200 = responses.get("200");
        res200.setDescription("请求成功，响应成功");
        ApiResponse res100 = new ApiResponse();
        res100.description("请求成功，待响应");
        res100.set$ref(res200.get$ref());
        responses.addApiResponse("100", res100);
        ApiResponse res300 = new ApiResponse();
        res300.description("请求成功，待重定向");
        res300.set$ref(res200.get$ref());
        responses.addApiResponse("300", res300);
        ApiResponse res400 = new ApiResponse();
        res400.description("请求失败，参数异常");
        res400.set$ref(res200.get$ref());
        responses.addApiResponse("400", res400);
        ApiResponse res404 = new ApiResponse();
        res404.description("请求失败，找不到资源");
        res404.set$ref(res200.get$ref());
        responses.addApiResponse("404", res404);
        ApiResponse res500 = new ApiResponse();
        res500.description("请求成功，响应失败");
        res500.set$ref(res200.get$ref());
        responses.addApiResponse("500", res500);
    }

    @SuppressWarnings("SameParameterValue")
    private void addExtensionParameters(String[] params, String extensionName, Operation operation) {
        if (params != null && params.length > 0) {
            Map<String, Boolean> map = new HashMap<>(8);
            for (String ignore : params) {
                if (ignore != null && !"".equals(ignore) && !"null".equals(ignore)) {
                    map.put(ignore, true);
                }
            }
            operation.addExtension(extensionName, map);
        }
    }

    private void addExtensionParameters(String[] params, Operation operation) {
        if (params != null && params.length > 0) {
            List<Parameter> parameters = new ArrayList<>();
            Map<String, Boolean> map = new HashMap<>(8);
            for (String ignore : params) {
                if (ignore != null && !ignore.isEmpty() && !"null".equals(ignore)) {
                    Parameter para = new Parameter();
                    para.setDeprecated(true);
                    para.setName(ignore);
                    parameters.add(para);
                }
            }
            operation.parameters(parameters);
        }
    }
}