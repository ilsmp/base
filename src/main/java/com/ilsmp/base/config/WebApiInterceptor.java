package com.ilsmp.base.config;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import com.ilsmp.base.util.ServletUtil;
import com.ilsmp.base.util.TimeUtil;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 我们在提供 API 的时候，经常需要对 API 进行统一的拦截，比如进行接口的安全性校验。创建一个拦截器类：WebApiInterceptor，并实现 HandlerInterceptor 接口
 */
@Slf4j
public class WebApiInterceptor implements HandlerInterceptor {

    /**
     * 请求之前
     *
     * @param httpServletRequest
     *         Request
     * @param httpServletResponse
     *         Response
     * @param o
     *         Object
     * @return true
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                             @NotNull Object o) throws Exception {
        System.out.println("进入拦截器,请求之前");
        // 打印参数
        printHeaderAndBody(httpServletRequest, httpServletResponse);
        return true;
    }

    private void printHeaderAndBody(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        // 添加请求id
        String requestId = TimeUtil.obtainCurrentTimeNum() + "_"
                + httpServletRequest.getRemoteAddr() + "_"
                + httpServletRequest.getRemoteUser() + "_"
                + httpServletRequest.getMethod() + "_"
                + httpServletRequest.getRequestURI() + "_"
                + UUID.randomUUID();
        httpServletResponse.addHeader("request-id", requestId);

        // 打印参数
        if (httpServletRequest instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) httpServletRequest;
            log.info("\n===request-id:" + requestId
                    + "\n===header:" + ServletUtil.getRequestHeaderString(requestWrapper)
                    + "\n===param:" + ServletUtil.getRequestParamString(requestWrapper)
                    + "\n===body:" + ServletUtil.getRequestBodyString(requestWrapper));
        }
    }

    /**
     * 请求时
     *
     * @param httpServletRequest
     *         Request
     * @param httpServletResponse
     *         Response
     * @param o
     *         Object
     * @param modelAndView
     *         modelAndView
     */
    @Override
    public void postHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse
            httpServletResponse, @NotNull Object o, ModelAndView modelAndView) {
        System.out.println("进入拦截器,请求时");
    }

    /**
     * 请求完成
     *
     * @param httpServletRequest
     *         Request
     * @param httpServletResponse
     *         Response
     * @param o
     *         Object
     * @param e
     *         Exception
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse
            httpServletResponse, @NotNull Object o, Exception e) {
        System.out.println("进入拦截器,请求完成");
    }
}
