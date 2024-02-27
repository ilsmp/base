package com.ilsmp.base.config;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.Response;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author:: ZJH Title: Amend Package com.zhihui.gongdi.po Description: 接口说明文档，整个工程有继承
 * WebMvcConfigurationSupport类，就需要解决**资源无法访问问题，重写addResourceHandlers方法 否则无法使用swagger-UI 应用主类添加注解@EnableOpenApi
 * (swagger2是@EnableSwagger2) 访问地址：http://localhost:port/swagger-ui/index.html(swagger2是http://localhost:port/swagger-ui.html)
 * spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER(actuator支持properties中修改项目路径匹配方式) Date 2019/10/8 16:08
 */
@Configuration
@EnableOpenApi
@ConditionalOnWebApplication
public class SwaggerConfig {

    @Value("${spring.base.api-enable:true}")
    private Boolean swaggerEnable;

    @Value("${spring.base.user-id:user-id}")
    private String userId;

    private final ParameterType input = ParameterType.HEADER;

    @Bean
    public Docket schemeApi() {
        return baseDocket()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.regex("^(?=.*?/zjh/).*?/doc$"))
                .build()
                .globalResponses(HttpMethod.GET, getGlobalResponse())
                .globalResponses(HttpMethod.POST, getGlobalResponse())
                .globalResponses(HttpMethod.PUT, getGlobalResponse())
                .globalResponses(HttpMethod.DELETE, getGlobalResponse())
                .globalResponses(HttpMethod.HEAD, getGlobalResponse())
                .globalResponses(HttpMethod.PATCH, getGlobalResponse())
                .globalResponses(HttpMethod.OPTIONS, getGlobalResponse())
                .globalResponses(HttpMethod.TRACE, getGlobalResponse())
                // 设置全局授权参数值
                .securitySchemes(securitySchemes())
                // 设置全局授权参数值生效url
                .securityContexts(securityContexts())
                // 分组方式
                .groupName("schemeApi")
                .ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class)
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .enable(swaggerEnable);
    }

    @Bean()
    public Docket publicApi() {
        return baseDocket()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.regex("^(?!.*?/zjh/).*$"))
                .build()
                .globalResponses(HttpMethod.GET, getGlobalResponse())
                .globalResponses(HttpMethod.POST, getGlobalResponse())
                .globalResponses(HttpMethod.PUT, getGlobalResponse())
                .globalResponses(HttpMethod.DELETE, getGlobalResponse())
                .globalResponses(HttpMethod.HEAD, getGlobalResponse())
                .globalResponses(HttpMethod.PATCH, getGlobalResponse())
                .globalResponses(HttpMethod.OPTIONS, getGlobalResponse())
                .globalResponses(HttpMethod.TRACE, getGlobalResponse())
                // 生成全局通用参数输入框
                .globalRequestParameters(globalRequestParameters())
                // 分组方式
                .groupName("publicApi")
                .ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class)
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false)
                .enable(swaggerEnable);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("接口说明文档")
                .description("线上项目开发测试")
                .license("")
                .licenseUrl("")
                .version("1.0.0")
                .contact(new Contact("张家浩", ".....", "981163085@qq.com"))
                .build();
    }

    private ApiSelectorBuilder baseDocket() {
        return new Docket(DocumentationType.OAS_30)
                .directModelSubstitute(LocalDateTime.class, Date.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalTime.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(ZonedDateTime.class, String.class)
                .directModelSubstitute(Period.class, String.class)
                .directModelSubstitute(Timestamp.class, Long.class)
                .select();
    }

    /*
     * Description: 生成全局通用参数输入框
     **/
    private List<RequestParameter> globalRequestParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        parameters.add(new RequestParameterBuilder()
                // 全局header参数
                .name(userId)
                .description("用户id")
                .required(false)
                .in(input)
                .query(q -> {
                    q.defaultValue("1234");
                    q.allowEmptyValue(false);
                    q.model(m -> m.scalarModel(ScalarType.STRING));
                }).build());
        return parameters;
    }

    /*
     * Description: 生成通用响应信息
     **/
    private List<Response> getGlobalResponse() {
        List<Response> responseList = new ArrayList<>();
        responseList.add(new ResponseBuilder().code("100").description("请求成功，待响应").build());
        responseList.add(new ResponseBuilder().code("200").description("请求成功，响应成功").build());
        responseList.add(new ResponseBuilder().code("300").description("请求成功，待重定向").build());
        responseList.add(new ResponseBuilder().code("400").description("请求失败，参数异常").build());
        responseList.add(new ResponseBuilder().code("404").description("请求失败，找不到资源").build());
        responseList.add(new ResponseBuilder().code("500").description("请求成功，响应失败").build());
        return responseList;
    }

    /**
     * 设置全局授权参数值
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> apiKeys = new ArrayList<>();
        apiKeys.add(new ApiKey(userId, "globalValue", input.getIn()));
        return apiKeys;
    }

    /*
     * Description: 设置全局授权参数值生效url
     **/
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        AuthorizationScope authorizationScope = new AuthorizationScope("zjh", "访问所有接口");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference(userId, authorizationScopes));
        securityContexts.add(SecurityContext.builder().securityReferences(securityReferences)
                .forPaths(PathSelectors.regex("^(?!test).*$"))
                .build());
        return securityContexts;
    }

    /**
     * 增加如下配置可解决
     * Spring Boot 2.6.x 与Swagger 3.0.0 不兼容问题
     **/
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
            WebEndpointsSupplier webEndpointsSupplier,
            ServletEndpointsSupplier servletEndpointsSupplier,
            ControllerEndpointsSupplier controllerEndpointsSupplier,
            EndpointMediaTypes endpointMediaTypes,
            CorsEndpointProperties corsProperties,
            WebEndpointProperties webEndpointProperties,
            Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = webEndpointProperties.getDiscovery().isEnabled()
                        && (StringUtils.hasText(basePath)
                        || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping);
    }

}


