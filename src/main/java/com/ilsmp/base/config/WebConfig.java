package com.ilsmp.base.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ilsmp.base.util.JsonUtil;
import jakarta.servlet.ServletContext;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.providers.ActuatorProvider;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerResourceResolver;
import org.springdoc.webmvc.ui.SwaggerWebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

/**
 * addInterceptors 方法，将 WebApiInterceptor 拦截器类添加进去
 */
@Configuration
public class WebConfig extends SwaggerWebMvcConfigurer implements ApplicationContextAware, ServletContextAware {

    @Value("${spring.base.allow-age:3600}")
    private Long maxAge;
    @Value("${spring.base.allow-credential:true}")
    private Boolean allowCredential;
    @Value("#{'${spring.base.allow-method:GET,POST,PUT,PATCH,DELETE,OPTIONS}'.split(',')}")
    private String[] allowedMethods;
    @Value("#{'${spring.base.allow-header:Origin,Content-Type,Accept,request-id,token,user-id}'.split(',')}")
    private String[] allowedHeaders;
    @Value("#{'${spring.base.allow-origin:*}'.split(',')}")
    private String[] allowOrigins;
    @Nullable
    private ApplicationContext applicationContext;
    @Nullable
    private ServletContext servletContext;

    public WebConfig(SwaggerUiConfigParameters swaggerUiConfigParameters, SwaggerIndexTransformer swaggerIndexTransformer, Optional<ActuatorProvider> actuatorProvider, SwaggerResourceResolver swaggerResourceResolver) {
        super(swaggerUiConfigParameters, swaggerIndexTransformer, actuatorProvider, swaggerResourceResolver);
    }


    /*
     * 请求拦截器WebApiInterceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WebApiInterceptor());
    }

    public void addCorsMappings(CorsRegistry registry) {
        registry
                // 开启跨域请求校验的path
                .addMapping("/**")
                // 允许的跨域白名单(allowCredentials=true:allowedOriginPatterns)
                // .allowedOrigins(allowOrigins)
                .allowedOriginPatterns(allowOrigins)
                // 允许哪些类型请求
                .allowedMethods(allowedMethods)
                // 允许的请求头
                .allowedHeaders(allowedHeaders)
                // 自定义请求头
                .exposedHeaders("zjh")
                // 最大超时时间s
                .maxAge(maxAge)
                // 是否允许携带cookie
                .allowCredentials(allowCredential);
    }

    /*
     * Author: zhangjiahao04
     * Description:swagger-ui.html路径映射，浏览器中使用/api-docs访问
     * Date: 2021/10/28 22:00
     **/
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/zjh", "/swagger-ui.html");
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        // 解决继承 WebMvcConfigurationSupport类静态资源无法访问
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        // 解决swagger无法访问
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/**");
        registry.addResourceHandler("/v2/api-docs")
                .addResourceLocations("classpath:/*/v2/api-docs");
        registry.addResourceHandler("/v3/api-docs")
                .addResourceLocations("classpath:/*/v3/api-docs");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
    }

    /**
     * spring boot 2.0 传参pageable报错解决办法,注册Spring data jpa pageable的参数分解器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
    }

    /**
     * 返回的字符串默认使用 Jackson 引擎，我们可以自定义JSON引擎
     * 重写configureMessageConverters 方法，可以将JSON引擎替换为fastJSON。
     */
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new ResourceRegionHttpMessageConverter());
        converters.forEach(con -> {
            if (con instanceof ByteArrayHttpMessageConverter) {
                ((ByteArrayHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof ResourceHttpMessageConverter) {
                ((ResourceHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof ResourceRegionHttpMessageConverter) {
                ((ResourceRegionHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof SourceHttpMessageConverter) {
                ((SourceHttpMessageConverter<?>) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof AllEncompassingFormHttpMessageConverter) {
                ((AllEncompassingFormHttpMessageConverter) con).setMultipartCharset(StandardCharsets.UTF_8);
            } else if (con instanceof Jaxb2RootElementHttpMessageConverter) {
                ((Jaxb2RootElementHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof MappingJackson2HttpMessageConverter jackson) {
                // 替换json消息转换器
                jackson.setObjectMapper(JsonUtil.getInstance());
                // 新增text_plain类型,避免遇到响应体是json,而响应头是text/plain类型无法找到适合的解析
                List<MediaType> mediaTypes = new ArrayList<>(jackson.getSupportedMediaTypes());
                mediaTypes.add(MediaType.TEXT_PLAIN);
                jackson.setSupportedMediaTypes(mediaTypes);
                jackson.setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {

    }

    @Nullable
    public final ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Nullable
    public final ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setServletContext(@Nullable ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
