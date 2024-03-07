package com.ilsmp.base.config;

import com.github.xiaoymin.knife4j.spring.extension.Knife4jJakartaOperationCustomizer;
import com.ilsmp.base.util.StringUtil;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Description: 忽略参数 Package: com.ilsmp.base.config Author: zhangjiahao04 Title: PageIgnoreOperation Date: 2024/3/3
 * 19:27
 */

@Component
public class PageIgnoreOperation extends Knife4jJakartaOperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        super.customize(operation, handlerMethod);
        setGlobalResponse(operation);
        if (hasPage(handlerMethod)) {
            addExtensionParameters(operation, handlerMethod);
        }
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

    private void addExtensionParameters(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getParameters() != null) {
            operation.getParameters().removeIf(parameter -> parameter.getName().startsWith("page"));
        } else {
            Content content = operation.getRequestBody().getContent();
            String ref = content.get("application/json").getSchema().get$ref();
            if (ref != null)  {
                if (ref.endsWith("Pageable")) {
                    content.remove("application/json");
                }
            } else {
                var properties = content.get("application/json").getSchema().getProperties();
                if (!StringUtil.isEmpty(properties)) {
                    properties.remove("pageable");
                }
            }
        }
        Parameter para1 = new Parameter();
        para1.setStyle(Parameter.StyleEnum.FORM);
        para1.setDescription("第几页，从0开始，默认为第0页");
        para1.setName("page");
        para1.setExample(0);
        Parameter para2 = new Parameter();
        para2.setStyle(Parameter.StyleEnum.FORM);
        para2.setDescription("每一页的大小，默认为10");
        para2.setName("size");
        para2.setExample(10);
        Parameter para3 = new Parameter();
        para3.setStyle(Parameter.StyleEnum.FORM);
        para3.setDescription("按属性排序,按括号内格式填写:(属性,asc|desc)");
        para3.setName("sort");
        para3.setExample("id,desc");
        operation.addParametersItem(para1);
        operation.addParametersItem(para2);
        operation.addParametersItem(para3);

    }

//    @Bean
//    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
//        return openApi -> {
//            openApi.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION)).components(openApi.getComponents().addSecuritySchemes(HttpHeaders.AUTHORIZATION, new SecurityScheme().type(SecurityScheme.Type.OAUTH2).name(HttpHeaders.AUTHORIZATION).in(SecurityScheme.In.HEADER).flows(new OAuthFlows().password(new OAuthFlow().tokenUrl("http://localhost:8082/user/info")))));
//        };
//    }
}