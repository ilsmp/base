package com.ilsmp.base.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author:: ZJH Title: Amend Package com.zhihui.gongdi.po Description: 接口说明文档，整个工程有继承
 * WebMvcConfigurationSupport类，就需要解决**资源无法访问问题，重写addResourceHandlers方法 否则无法使用swagger-UI 应用主类添加注解@EnableOpenApi
 * (swagger2是@EnableSwagger2)
 * 访问地址：http://localhost:port/swagger-ui/index.html(swagger2是http://localhost:port/swagger-ui.html)
 * spring.mvc.pathmatch.matching-strategy=ANT_PATH_MATCHER(actuator支持properties中修改项目路径匹配方式) Date 2019/10/8 16:08
 */
@Configuration
@ConditionalOnWebApplication
public class SwaggerConfig {

    // 创建 token API分组
//    @Bean
//    public GroupedOpenApi tokenApi() {
//        return GroupedOpenApi.builder()
//                .group("token接口")
//                .pathsToMatch("/token/**")
//                .build();
//    }
//
//    // 创建 jwt API分组
//    @Bean
//    public GroupedOpenApi JWTApi() {
//        return GroupedOpenApi.builder().packagesToScan()
//                .group("jwt接口")
//                .pathsToMatch("/jwt/**").packagesToScan()
//                .build();
//    }

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                // 接口文档标题
                .info(new Info().title("接口说明文档")
                                // 接口文档简介
                                .description("线上项目开发测试")
                                // 接口文档版本
                                .version("v1.0")
                                // 开发者联系方式
                                .contact(new Contact().name("zjh").email("981163085@qq.com"))
                );
    }
//.externalDocs(new ExternalDocumentation().description("配置springBoot的基础配置").url("https://mvnrepository.com/artifact/com.ilsmp/base-spring-boot-starter"))

}


