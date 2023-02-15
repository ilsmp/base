package com.ilsmp.base.config;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.ilsmp.base.util.RequestWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author zjh
 * @Title: 跨域访问
 * @Description: 跨域访问
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCORSFilter implements Filter {

    @Value("${spring.base.allow-age:3600}")
    private Long maxAge;
    @Value("${spring.base.allow-credential:true}")
    private Boolean allowCredential;
    @Value("#{'${spring.base.allow-method:GET,POST,PUT,PATCH,DELETE,OPTIONS}'.split(',')}")
    private List<String> allowedMethods;
    @Value("#{'${spring.base.allow-header:Origin,Content-Type,Accept,request-id,token,user-id}'.split(',')}")
    private List<String> allowedHeaders;
    @Value("#{'${spring.base.allow-origin:*}'.split(',')}")
    private List<String> allowOrigins;

    private static final String FORM_MULTIPART_TYPE = "multipart/form-data";
    private static final String FORM_WWW_TYPE = "application/x-www-form-urlencoded";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
            IOException, ServletException {
        servletRequest.setCharacterEncoding("UTF-8");
        servletResponse.setCharacterEncoding("UTF-8");
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                String contentType = request.getContentType();
                if (contentType != null &&
                        (contentType.contains(FORM_MULTIPART_TYPE) || contentType.contains(FORM_WWW_TYPE))) {
                    filterChain.doFilter(request, response);
                } else {
                    RequestWrapper requestWrapper = new RequestWrapper(request);
                    filterChain.doFilter(requestWrapper, response);
                }
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /*
     * Author: zhangjiahao04
     * Description: 当集成第三方权限认证组件时使用
     * Date: 2022/3/9 18:40
     * Param:
     * return:
     **/
    // @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许的跨域白名单(allowCredentials=true:allowedOriginPatterns)
        // corsConfiguration.setAllowedOrigins(allowOrigins);
        corsConfiguration.setAllowedOriginPatterns(allowOrigins);
        // 允许的请求头
        corsConfiguration.setAllowedHeaders(allowedHeaders);
        // 自定义请求头
        corsConfiguration.addExposedHeader("zjh");
        // 允许哪些类型请求
        corsConfiguration.setAllowedMethods(allowedMethods);
        // 是否允许携带cookie
        corsConfiguration.setAllowCredentials(allowCredential);
        // 最大超时时间s
        corsConfiguration.setMaxAge(maxAge);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 开启跨域请求校验的path
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Override
    public void destroy() {
    }
}
