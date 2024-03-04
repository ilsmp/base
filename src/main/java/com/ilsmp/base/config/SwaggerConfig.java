package com.ilsmp.base.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;


/**
 * @author:: ZJH Title: Amend Package com.zhihui.gongdi.po Description: 接口说明文档，整个工程有继承
 * WebMvcConfigurationSupport类，就需要解决**资源无法访问问题，重写addResourceHandlers方法 否则无法使用swagger-UI 应用主类添加注解@EnableOpenApi
 * (swagger2是@EnableSwagger2) 访问地址：http://localhost:port/swagger-ui/index.html(swagger2是http://localhost:port/swagger-ui.html)
 * spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER(actuator支持properties中修改项目路径匹配方式) Date 2019/10/8 16:08
 */
@Configuration
@ConditionalOnWebApplication
public class SwaggerConfig {

    // 创建 token API分组
    @Bean
    public GroupedOpenApi tokenApi() {
        return GroupedOpenApi.builder()
                .group("token接口")
                .pathsToMatch("/token/**")
                .build();
    }

    // 创建 jwt API分组
    @Bean
    public GroupedOpenApi JWTApi() {
        return GroupedOpenApi.builder().packagesToScan()
                .group("jwt接口")
                .pathsToMatch("/jwt/**").packagesToScan()
                .build();
    }

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                // 接口文档标题
                .info(
                        new Info().title("接口说明文档")
                                // 接口文档简介
                                .description("线上项目开发测试")
                                // 接口文档版本
                                .version("v1.0")
                                // 开发者联系方式
                                .contact(new Contact().name("zjh").email("981163085@qq.com"))
                ).externalDocs(new ExternalDocumentation().description("配置springBoot的基础配置")
                        .url("https://mvnrepository.com/artifact/com.ilsmp/base-spring-boot-starter"));
    }

    /**
     * 增加如下配置可解决
     * Spring Boot 2.6.x 与Swagger 3.0.0 不兼容问题
     **/
    @Bean
    @ConditionalOnProperty(name = "spring.base.swagger3-adapter", matchIfMissing = false)
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


