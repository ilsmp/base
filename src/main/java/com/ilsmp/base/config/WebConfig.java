package com.ilsmp.base.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ilsmp.base.util.JsonUtil;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.SpringProperties;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.JsonbHttpMessageConverter;
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.smile.MappingJackson2SmileHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.method.support.CompositeUriComponentsContributor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 整个工程有继承 WebMvcConfigurationSupport类，就需要解决**资源无法访问问题，重写addResourceHandlers方法 进行各种自定义配置，
 * 如拦截器ApiInterceptor，接口版本控制ApiVersion.java，json数据解析等 SpringBootConfiguration 注解的类继承 WebMvcConfigurationSupport 类，并重写
 * addInterceptors 方法，将 WebApiInterceptor 拦截器类添加进去，代码如下：
 */
@Configuration
public class WebConfig implements WebMvcConfigurer, ApplicationContextAware, ServletContextAware {
    /**
     * Boolean flag controlled by a {@code spring.xml.ignore} system property that instructs Spring to ignore XML, i.e.
     * to not initialize the XML-related infrastructure.
     * <p>The default is "false".
     */
    private static final boolean shouldIgnoreXml = SpringProperties.getFlag("spring.xml.ignore");
    private static final boolean romePresent;
    private static final boolean jaxb2Present;
    private static final boolean jackson2Present;
    private static final boolean jackson2XmlPresent;
    private static final boolean jackson2SmilePresent;
    private static final boolean jackson2CborPresent;
    private static final boolean gsonPresent;
    private static final boolean jsonbPresent;
    private static final boolean kotlinSerializationJsonPresent;

    static {
        ClassLoader classLoader = WebMvcConfigurationSupport.class.getClassLoader();
        romePresent = ClassUtils.isPresent("com.rometools.rome.feed.WireFeed", classLoader);
        jaxb2Present = ClassUtils.isPresent("javax.xml.bind.Binder", classLoader);
        jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
                classLoader) &&
                ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", classLoader);
        jackson2XmlPresent = ClassUtils.isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper",
                classLoader);
        jackson2SmilePresent = ClassUtils.isPresent("com.fasterxml.jackson.dataformat.smile.SmileFactory",
                classLoader);
        jackson2CborPresent = ClassUtils.isPresent("com.fasterxml.jackson.dataformat.cbor.CBORFactory",
                classLoader);
        gsonPresent = ClassUtils.isPresent("com.google.gson.Gson", classLoader);
        jsonbPresent = ClassUtils.isPresent("javax.json.bind.Jsonb", classLoader);
        kotlinSerializationJsonPresent = ClassUtils.isPresent("kotlinx.serialization.json.Json",
                classLoader);
    }

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
    @Nullable
    private PathMatchConfigurer pathMatchConfigurer;
    @Nullable
    private List<HandlerMethodArgumentResolver> argumentResolvers;
    @Nullable
    private List<HandlerMethodReturnValueHandler> returnValueHandlers;
    @Nullable
    private List<HttpMessageConverter<?>> messageConverters;
    @Nullable
    private AsyncSupportConfigurer asyncSupportConfigurer;

    /**
     * 请求拦截器
     *
     * @return WebApiInterceptor
     */
    public WebApiInterceptor interceptor() {
        return new WebApiInterceptor();
    }

    /**
     * 接口版本控制 Protected method for plugging in a custom subclass of {@link RequestMappingHandlerMapping}.
     *
     * @since 4.0 * @return RequestMappingHandlerMapping
     */
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new ApiVersionCondition.CustomRequestMappingHandlerMapping();
    }

    /**
     * 拦截器 Override this method to add Spring MVC interceptors for pre- and post-processing of controller invocation.
     * @param registry
     * 拦截器
     * @see InterceptorRegistry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor());
    }

    /**
     * Override this method to add resource handlers for serving static resources.
     *
     * @see ResourceHandlerRegistry
     */
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决继承 WebMvcConfigurationSupport类静态资源无法访问
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        // 解决swagger无法访问
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/v2/api-docs")
                .addResourceLocations("classpath:/*/v2/api-docs");
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/**");
        registry.addResourceHandler("/v3/api-docs")
                .addResourceLocations("classpath:/*/v3/api-docs");
    }

    /**
     * Override this method to configure cross origin requests processing.
     *
     * @see CorsRegistry
     * @since 4.2
     */
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
     * Param:
     * return:
     * Override this method to add view controllers.
     * @see ViewControllerRegistry
     **/
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/zjh", "/swagger-ui/index.html");
    }

    /**
     * Add custom {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} to use in addition to the ones
     * registered by default.
     * <p>Custom argument resolvers are invoked before built-in resolvers except for
     * those that rely on the presence of annotations (e.g. {@code @RequestParameter}, {@code @PathVariable}, etc). The
     * latter can be customized by configuring the {@link RequestMappingHandlerAdapter} directly.
     * @param argumentResolvers
     * the list of custom converters (initially an empty list)
     * No primary or default constructor found for interface org.springframework.data.domain.Pageable
     * @param argumentResolvers
     * spring boot 2.0 传参pageable报错解决办法
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 注册Spring data jpa pageable的参数分解器时打开注释
        argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
    }

    /**
     * Override this method to add custom {@link HttpMessageConverter HttpMessageConverters} to use with the {@link
     * RequestMappingHandlerAdapter} and the {@link ExceptionHandlerExceptionResolver}.
     * <p>Adding converters to the list turns off the default converters that would
     * otherwise be registered by default. Also see {@link #addDefaultHttpMessageConverters} for adding default message
     * converters.
     * @param converters
     * a list to add message converters to (initially an empty list) 自定义 JSON 解析/编码 Spring Boot 中 RestController
     * 返回的字符串默认使用 Jackson 引擎，它也提供了工厂类，我们可以自定义 JSON 引擎，本节实例我们将 JSON 引擎替换为 fastJSON。 在 WebConfig
     * 类重写configureMessageConverters 方法
     * @return
     */
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        addDefaultHttpMessageConverters(converters);
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
                ((SourceHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof AllEncompassingFormHttpMessageConverter) {
                ((AllEncompassingFormHttpMessageConverter) con).setMultipartCharset(StandardCharsets.UTF_8);
            } else if (con instanceof Jaxb2RootElementHttpMessageConverter) {
                ((Jaxb2RootElementHttpMessageConverter) con).setDefaultCharset(StandardCharsets.UTF_8);
            } else if (con instanceof MappingJackson2HttpMessageConverter) {
                // 替换json消息转换器
                MappingJackson2HttpMessageConverter jackson = ((MappingJackson2HttpMessageConverter) con);
                jackson.setObjectMapper(JsonUtil.getInstance());
                // 新增text_plain类型,避免遇到响应体是json,而响应头是text/plain类型无法找到适合的解析
                List<MediaType> mediaTypes = new ArrayList<>(jackson.getSupportedMediaTypes());
                mediaTypes.add(MediaType.TEXT_PLAIN);
                jackson.setSupportedMediaTypes(mediaTypes);
                jackson.setDefaultCharset(StandardCharsets.UTF_8);
            }
        });
    }

    /**
     * Return the associated Spring {@link ApplicationContext}.
     *
     * @since 4.2
     */
    @Nullable
    public final ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * Set the Spring {@link ApplicationContext}, e.g. for resource loading.
     */
    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Return the associated {@link ServletContext}.
     * @since 4.2
     */
    @Nullable
    public final ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * Set the {@link ServletContext}, e.g. for resource handling, looking up file extensions, etc.
     */
    @Override
    public void setServletContext(@Nullable ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Callback for building the {@link PathMatchConfigurer}.
     * * Delegates to {@link #configurePathMatch}.
     * @since 4.1
     */
    protected PathMatchConfigurer getPathMatchConfigurer() {
        if (this.pathMatchConfigurer == null) {
            this.pathMatchConfigurer = new PathMatchConfigurer();
            configurePathMatch(this.pathMatchConfigurer);
        }
        return this.pathMatchConfigurer;
    }

    /**
     * Return a global {@link PathPatternParser} instance to use for parsing patterns to match to the {@link
     * org.springframework.http.server.RequestPath}. The returned instance can be configured using {@link
     * #configurePathMatch(PathMatchConfigurer)}.
     *
     * @since 5.3.4
     */
    @Bean
    public PathPatternParser mvcPatternParser() {
        return getPathMatchConfigurer().getPatternParserOrDefault();
    }

    /**
     * Protected method for plugging in a custom subclass of {@link RequestMappingHandlerAdapter}.
     *
     * @since 4.3
     */
    protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new RequestMappingHandlerAdapter();
    }

    /**
     * Return the {@link ConfigurableWebBindingInitializer} to use for initializing all {@link} instances.
     */
    protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer(
            FormattingConversionService mvcConversionService, Validator mvcValidator) {
        ConfigurableWebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
        initializer.setConversionService(mvcConversionService);
        initializer.setValidator(mvcValidator);
        MessageCodesResolver messageCodesResolver = getMessageCodesResolver();
        if (messageCodesResolver != null) {
            initializer.setMessageCodesResolver(messageCodesResolver);
        }
        return initializer;
    }

    /**
     * Return a {@link FormattingConversionService} for use with annotated controllers.
     * <p>See {@link #addFormatters} as an alternative to overriding this method.
     */
    @Bean
    public FormattingConversionService mvcConversionService() {
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        addFormatters(conversionService);
        return conversionService;
    }

    /**
     * Provide access to the shared custom argument resolvers used by the {@link RequestMappingHandlerAdapter} and the
     * {@link ExceptionHandlerExceptionResolver}.
     * <p>This method cannot be overridden; use {@link #addArgumentResolvers} instead.
     *
     * @since 4.3
     */
    protected final List<HandlerMethodArgumentResolver> getArgumentResolvers() {
        if (this.argumentResolvers == null) {
            this.argumentResolvers = new ArrayList<>();
            addArgumentResolvers(this.argumentResolvers);
        }
        return this.argumentResolvers;
    }

    /**
     * Provide access to the shared return value handlers used by the {@link RequestMappingHandlerAdapter} and the
     * {@link ExceptionHandlerExceptionResolver}.
     * <p>This method cannot be overridden; use {@link #addReturnValueHandlers} instead.
     *
     * @since 4.3
     */
    protected final List<HandlerMethodReturnValueHandler> getReturnValueHandlers() {
        if (this.returnValueHandlers == null) {
            this.returnValueHandlers = new ArrayList<>();
            addReturnValueHandlers(this.returnValueHandlers);
        }
        return this.returnValueHandlers;
    }

    /**
     * Provides access to the shared {@link HttpMessageConverter HttpMessageConverters} used by the {@link
     * RequestMappingHandlerAdapter} and the {@link ExceptionHandlerExceptionResolver}.
     * <p>This method cannot be overridden; use {@link #configureMessageConverters} instead.
     * Also see {@link #addDefaultHttpMessageConverters} for adding default message converters.
     */
    protected final List<HttpMessageConverter<?>> getMessageConverters() {
        if (this.messageConverters == null) {
            this.messageConverters = new ArrayList<>();
            configureMessageConverters(this.messageConverters);
            if (this.messageConverters.isEmpty()) {
                addDefaultHttpMessageConverters(this.messageConverters);
            }
            extendMessageConverters(this.messageConverters);
        }
        return this.messageConverters;
    }

    /**
     * Adds a set of default HttpMessageConverter instances to the given list. Subclasses can call this method from
     * {@link #configureMessageConverters}.
     *
     * @param messageConverters
     *         the list to add the default message converters to
     */
    protected final void addDefaultHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new ResourceRegionHttpMessageConverter());
        if (!shouldIgnoreXml) {
            try {
                messageConverters.add(new SourceHttpMessageConverter<>());
            } catch (Throwable ex) {
                // Ignore when no TransformerFactory implementation is available...
            }
        }
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        if (romePresent) {
            messageConverters.add(new AtomFeedHttpMessageConverter());
            messageConverters.add(new RssChannelHttpMessageConverter());
        }
        if (!shouldIgnoreXml) {
            if (jackson2XmlPresent) {
                Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.xml();
                if (this.applicationContext != null) {
                    builder.applicationContext(this.applicationContext);
                }
                messageConverters.add(new MappingJackson2XmlHttpMessageConverter(builder.build()));
            } else if (jaxb2Present) {
                messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
            }
        }
        if (kotlinSerializationJsonPresent) {
            messageConverters.add(new KotlinSerializationJsonHttpMessageConverter());
        }
        if (jackson2Present) {
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            if (this.applicationContext != null) {
                builder.applicationContext(this.applicationContext);
            }
            messageConverters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        } else if (gsonPresent) {
            messageConverters.add(new GsonHttpMessageConverter());
        } else if (jsonbPresent) {
            messageConverters.add(new JsonbHttpMessageConverter());
        }
        if (jackson2SmilePresent) {
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.smile();
            if (this.applicationContext != null) {
                builder.applicationContext(this.applicationContext);
            }
            messageConverters.add(new MappingJackson2SmileHttpMessageConverter(builder.build()));
        }
        if (jackson2CborPresent) {
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.cbor();
            if (this.applicationContext != null) {
                builder.applicationContext(this.applicationContext);
            }
            messageConverters.add(new MappingJackson2CborHttpMessageConverter(builder.build()));
        }
    }

    /**
     * Callback for building the {@link AsyncSupportConfigurer}. Delegates to {@link
     * #configureAsyncSupport(AsyncSupportConfigurer)}.
     *
     * @since 5.3.2
     */
    protected AsyncSupportConfigurer getAsyncSupportConfigurer() {
        if (this.asyncSupportConfigurer == null) {
            this.asyncSupportConfigurer = new AsyncSupportConfigurer();
            configureAsyncSupport(this.asyncSupportConfigurer);
        }
        return this.asyncSupportConfigurer;
    }

    /**
     * Return an instance of {@link CompositeUriComponentsContributor} for use with {@link
     * org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder}.
     *
     * @since 4.0
     */
    @Bean
    public CompositeUriComponentsContributor mvcUriComponentsContributor(
            @Qualifier("mvcConversionService") FormattingConversionService conversionService,
            @Qualifier("requestMappingHandlerAdapter") RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        return new CompositeUriComponentsContributor(
                requestMappingHandlerAdapter.getArgumentResolvers(), conversionService);
    }

    /**
     * Returns a {@link HttpRequestHandlerAdapter} for processing requests with {@link }.
     */
    @Bean
    public HttpRequestHandlerAdapter httpRequestHandlerAdapter() {
        return new HttpRequestHandlerAdapter();
    }

    /**
     * Returns a {@link SimpleControllerHandlerAdapter} for processing requests with interface-based controllers.
     */
    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    /**
     * Returns a {@link HandlerExceptionResolverComposite} containing a list of exception resolvers obtained either
     * through {@link #configureHandlerExceptionResolvers} or through {@link #addDefaultHandlerExceptionResolvers}.
     * <p><strong>Note:</strong> This method cannot be made final due to CGLIB constraints.
     * Rather than overriding it, consider overriding {@link #configureHandlerExceptionResolvers} which allows for
     * providing a list of resolvers.
     */
    @Bean
    public HandlerExceptionResolver handlerExceptionResolver(
            @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager) {
        List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
        configureHandlerExceptionResolvers(exceptionResolvers);
        if (exceptionResolvers.isEmpty()) {
            addDefaultHandlerExceptionResolvers(exceptionResolvers, contentNegotiationManager);
        }
        extendHandlerExceptionResolvers(exceptionResolvers);
        HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
        composite.setOrder(0);
        composite.setExceptionResolvers(exceptionResolvers);
        return composite;
    }

    /**
     * A method available to subclasses for adding default {@link HandlerExceptionResolver HandlerExceptionResolvers}.
     * <p>Adds the following exception resolvers:
     * <ul>
     * <li>{@link ExceptionHandlerExceptionResolver} for handling exceptions through
     * {@link org.springframework.web.bind.annotation.ExceptionHandler} methods.
     * <li>{@link ResponseStatusExceptionResolver} for exceptions annotated with
     * {@link org.springframework.web.bind.annotation.ResponseStatus}.
     * <li>{@link DefaultHandlerExceptionResolver} for resolving known Spring exception types
     * </ul>
     */
    protected final void addDefaultHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers,
                                                             ContentNegotiationManager mvcContentNegotiationManager) {
        ExceptionHandlerExceptionResolver exceptionHandlerResolver = createExceptionHandlerExceptionResolver();
        exceptionHandlerResolver.setContentNegotiationManager(mvcContentNegotiationManager);
        exceptionHandlerResolver.setMessageConverters(getMessageConverters());
        exceptionHandlerResolver.setCustomArgumentResolvers(getArgumentResolvers());
        exceptionHandlerResolver.setCustomReturnValueHandlers(getReturnValueHandlers());
        if (jackson2Present) {
            exceptionHandlerResolver.setResponseBodyAdvice(
                    Collections.singletonList(new JsonViewResponseBodyAdvice()));
        }
        if (this.applicationContext != null) {
            exceptionHandlerResolver.setApplicationContext(this.applicationContext);
        }
        exceptionHandlerResolver.afterPropertiesSet();
        exceptionResolvers.add(exceptionHandlerResolver);
        ResponseStatusExceptionResolver responseStatusResolver = new ResponseStatusExceptionResolver();
        responseStatusResolver.setMessageSource(this.applicationContext);
        exceptionResolvers.add(responseStatusResolver);
        exceptionResolvers.add(new DefaultHandlerExceptionResolver());
    }

    /**
     * Protected method for plugging in a custom subclass of {@link ExceptionHandlerExceptionResolver}.
     *
     * @since 4.3
     */
    protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
        return new ExceptionHandlerExceptionResolver();
    }

    @Bean
    @Lazy
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver();
    }

    @Bean
    public ThemeResolver themeResolver() {
        return new FixedThemeResolver();
    }

    @Bean
    public FlashMapManager flashMapManager() {
        return new SessionFlashMapManager();
    }

    @Bean
    public RequestToViewNameTranslator viewNameTranslator() {
        return new DefaultRequestToViewNameTranslator();
    }

}
