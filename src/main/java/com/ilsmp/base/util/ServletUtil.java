/*
 * Copyright (C) 2020 Baidu, Inc. All Rights Reserved.
 */
package com.ilsmp.base.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * ServletRequestUtil
 *
 * @author Xu Jun(xujun06@baidu.com)
 * @Date: 2020-08-27 10:32
 */
@Component
@Slf4j
public class ServletUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ThreadLocal<Map<String, String>> requestHeaders = new ThreadLocal<>();
    private static final ThreadLocal<Object> requestBody = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, String[]>> requestParams = new ThreadLocal<>();

    /**
     * url匹配
     */
    public static boolean urlMatch(String[] pathArray, String path) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        if (pathArray != null) {
            for (String pattern : pathArray) {
                if (antPathMatcher.match(pattern, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean urlMatchPattern(String url, String pattern) {
        if (pattern == null) {
            return false;
        }
        if (pattern.endsWith("**")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            if (url.startsWith(prefix)) {
                String suffix = url.substring(prefix.length());
                if (suffix.contains(",")) {
                    log.warn("[security warning] url contains ','(semicolon)  {}", url);
                    return false;
                }
                if (suffix.contains("../")) {
                    log.warn("[security warning] url contains '../'  {}", url);
                    return false;
                }
                return true;
            }
        }
        return pattern.equals(url);
    }

    // 获取ip
    public static String getLocalIP() {
        String localIP = "127.0.0.1";
        try {
            OK:
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                 interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        localIP = address.getHostAddress();
                        break OK;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return localIP;
    }

    // 通过request获取端口
    public static String getLocalPort() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getServerPort() + "";
    }

    // 通过request获取ip
    public static String getIp() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getServerName();
    }

    public static String getIpAndPort() {
        return getLocalIP() + ":" + getLocalPort();
    }

    /**
     * 获取headers
     */
    public static Map<String, String> getRequestHeader() {
        Map<String, String> headerMap = requestHeaders.get();
        if (StringUtil.isEmpty(headerMap)) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                headerMap = new HashMap<>();
                headerMap.put("noHeaderRequest", null);
                requestHeaders.set(headerMap);
                return headerMap;
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
            return getRequestHeader(wrapperRequest);
        }
        return headerMap;
    }

    public static Map<String, String> getRequestHeader(ContentCachingRequestWrapper request) {
        Map<String, String> header = requestHeaders.get();
        if (StringUtil.isEmpty(header) && request != null) {
            Enumeration<String> headerNames = request.getHeaderNames();
            header = new HashMap<>();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                String value = request.getHeader(key);
                header.put(key, value);
            }
            if (StringUtil.isEmpty(header)) {
                header.put("noHeaderParam", null);
            }
            requestHeaders.set(header);
        }
        return header;
    }

    public static String getRequestHeader(String headerName) {
        Map<String, String> kv = getRequestHeader();
        assert kv != null;
        return kv.getOrDefault(headerName.toLowerCase(), "");
    }

    public static String getRequestHeaderDecode(String headerName) {
        Map<String, String> kv = getRequestHeader();
        assert kv != null;
        return StringUtil.decodeFromString(kv.getOrDefault(headerName.toLowerCase(), ""));
    }

    public static String getRequestHeaderString() throws JsonProcessingException {
        Map<String, String> headerMap = requestHeaders.get();
        if (StringUtil.isEmpty(headerMap)) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                headerMap = new HashMap<>();
                headerMap.put("noHeaderRequest", null);
                requestHeaders.set(headerMap);
                return "";
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
            return getRequestHeaderString(wrapperRequest);
        }
        return MAPPER.writeValueAsString(headerMap);
    }

    public static String getRequestHeaderString(ContentCachingRequestWrapper request) throws JsonProcessingException {
        return MAPPER.writeValueAsString(getRequestHeader());
    }

    /**
     * 获取 param 请求参数
     */
    public static Map<String, String[]> getRequestParam() {
        Map<String, String[]> paramMap = requestParams.get();
        if (StringUtil.isEmpty(paramMap)) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                paramMap = new HashMap<>();
                paramMap.put("noParamRequest", null);
                requestParams.set(paramMap);
                return paramMap;
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
            return getRequestParam(wrapperRequest);
        }
        return paramMap;
    }

    public static Map<String, String[]> getRequestParam(ContentCachingRequestWrapper request) {
        Map<String, String[]> paramMap = requestParams.get();
        if (StringUtil.isEmpty(paramMap) && request != null) {
            paramMap = request.getParameterMap();
            if (StringUtil.isEmpty(paramMap)) {
                paramMap = new HashMap<>();
                paramMap.put("noParam", null);
            }
            requestParams.set(paramMap);
        }
        return paramMap;
    }

    public static String getRequestParam(String paramName) {
        Map<String, String[]> kv = getRequestParam();
        assert kv != null;
        String[] strings = kv.getOrDefault(paramName, new String[]{});
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= strings.length; i++) {
            builder.append(builder);
            if (i != strings.length) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public static String getRequestParamDecode(String paramName) {
        Map<String, String[]> kv = getRequestParam();
        assert kv != null;
        return StringUtil.decodeFromString(Arrays.toString(kv.getOrDefault(paramName, new String[]{""})));
    }

    public static Object getRequestObject(String name) throws IOException {
        Object result = getRequestHeader(name);
        if (StringUtil.isEmpty(result)) {
            result = getRequestParam(name);
            if (StringUtil.isEmpty(result)) {
                result = getRequestBody(name);
                if (StringUtil.isEmpty(result)) {
                    return "";
                }
            }
        }
        return result;
    }

    public static Object getRequestObjectDecode(String name) throws IOException {
        Object result = getRequestHeaderDecode(name);
        if (StringUtil.isEmpty(result)) {
            result = getRequestBodyDecode(name);
            if (StringUtil.isEmpty(result)) {
                result = getRequestParamDecode(name);
            }
        }
        return result;
    }

    public static String getRequestParamString() throws JsonProcessingException {
        Map<String, String[]> paramMap = requestParams.get();
        if (StringUtil.isEmpty(paramMap)) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                paramMap = new HashMap<>();
                paramMap.put("noParamRequest", null);
                requestParams.set(paramMap);
                return "";
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
            return getRequestParamString(wrapperRequest);
        }
        return MAPPER.writeValueAsString(paramMap);
    }

    public static String getRequestParamString(ContentCachingRequestWrapper request) throws JsonProcessingException {
        return MAPPER.writeValueAsString(getRequestParam(request));
    }

    /**
     * 获取 body 请求参数
     */
    public static Object getRequestBody() throws IOException {
        Object body = requestBody.get();
        if (StringUtil.isEmpty(body)) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                body = Collections.singleton(new HashMap<String, Object>() {{
                    put("noBodyRequest", null);
                }});
                requestBody.set(body);
                return body;
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
            return getRequestBody(wrapperRequest);
        }
        return body;
    }

    public static Object getRequestBody(HttpServletRequest request) throws IOException {
        Object body = requestBody.get();
        if (StringUtil.isEmpty(body)) {
            getRequestBodyString(request);
            return requestBody.get();
        }
        return body;
    }

    public static Object getRequestBody(String bodyName) throws IOException {
        Object body = getRequestBody();
        if (body instanceof Map) {
            Map<String, Object> kv = (Map<String, Object>) getRequestBody();
            assert kv != null;
            return kv.getOrDefault(bodyName, "");
        } else {
            return "";
        }
    }

    public static Object getRequestBodyDecode(String bodyName) throws IOException {
        Object body = getRequestBody();
        if (body instanceof Map) {
            Map<String, Object> kv = (Map<String, Object>) getRequestBody();
            assert kv != null;
            return StringUtil.decodeFromString(JsonUtil.writeJsonStr(kv.getOrDefault(bodyName, "")));
        } else {
            return "";
        }
    }

    public static String getRequestBodyString() throws IOException {
        Object body = requestBody.get();
        if (StringUtil.isEmpty(body)) {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                body = Collections.singleton(new HashMap<String, Object>() {{
                    put("noBodyRequest", null);
                }});
                requestBody.set(body);
                return "";
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            ContentCachingRequestWrapper wrapperRequest = new ContentCachingRequestWrapper(request);
            return getRequestBodyString(wrapperRequest);
        }
        return MAPPER.writeValueAsString(body);
    }

    public static String getRequestBodyString(HttpServletRequest request) throws IOException {
        Object body = requestBody.get();
        if (StringUtil.isEmpty(body) && request != null) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            String toString = sb.toString();
            if (StringUtil.isEmpty(toString)) {
                body = Collections.singleton(new HashMap<String, Object>() {{
                    put("noBodyParam", null);
                }});
            } else {
                try {
                    body = MAPPER.readValue(toString, Collection.class);
                } catch (Exception e) {
                    try {
                        body = MAPPER.readValue(toString, Map.class);
                    } catch (Exception c) {
                        body = toString.split("&");
                    }
                }
            }
            requestBody.set(body);
            return toString;
        }
        return MAPPER.writeValueAsString(body);
    }

    /**
     * 流拷贝
     */
    public static List<InputStream> cloneInputStream(InputStream input) throws IOException {
        byte[] buffer = toByteArray(input);
        input.close();
        List<InputStream> list = new ArrayList<>();
        list.add(new ByteArrayInputStream(buffer));
        list.add(new ByteArrayInputStream(buffer));
        log.warn("----流拷贝两份成功--byte[]长度---{}---", buffer.length);
        return list;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[1024 * 4]);
    }

    public static long copyLarge(Reader input, Writer output, char[] buffer) throws IOException {
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void remove() {
        requestHeaders.remove();
        requestParams.remove();
        requestBody.remove();
    }


    /**
     * 设置客户端缓存过期时间 的Header.
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        // Http 1.0 header, set a fix expires date.
        response.setDateHeader(HttpHeaders.EXPIRES, System.currentTimeMillis() + expiresSeconds * 1000);
        // Http 1.1 header, set a time after now.
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=" + expiresSeconds);
    }

    /**
     * 设置禁止客户端缓存的Header.
     */
    public static void setNoCacheHeader(HttpServletResponse response) {
        // Http 1.0 header
        response.setDateHeader(HttpHeaders.EXPIRES, 1L);
        response.addHeader(HttpHeaders.PRAGMA, "no-cache");
        // Http 1.1 header
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0");
    }

    /**
     * 设置LastModified Header.
     */
    public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedDate) {
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModifiedDate);
    }

    /**
     * 设置Etag Header.
     */
    public static void setEtag(HttpServletResponse response, String etag) {
        response.setHeader(HttpHeaders.ETAG, etag);
    }

    /**
     * 根据浏览器If-Modified-Since Header, 计算文件是否已被修改. 如果无修改, checkIfModify返回false ,设置304 not modify status.
     *
     * @param lastModified
     *         内容的最后修改时间.
     */
    public static boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response,
                                               long lastModified) {
        long ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        if ((ifModifiedSince != -1) && (lastModified < ifModifiedSince + 1000)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return false;
        }
        return true;
    }

    /**
     * 根据浏览器 If-None-Match Header, 计算Etag是否已无效. 如果Etag有效, checkIfNoneMatch返回false, 设置304 not modify status.
     *
     * @param etag
     *         内容的ETag.
     */
    public static boolean checkIfNoneMatchEtag(HttpServletRequest request, HttpServletResponse response, String etag) {
        String headerValue = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (headerValue != null) {
            boolean conditionSatisfied = false;
            if (!"*".equals(headerValue)) {
                StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(etag)) {
                        conditionSatisfied = true;
                    }
                }
            } else {
                conditionSatisfied = true;
            }
            if (conditionSatisfied) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setHeader(HttpHeaders.ETAG, etag);
                return false;
            }
        }
        return true;
    }

    /**
     * 是否是Ajax异步请求
     *
     * @param request
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            return true;
        }
        String uri = request.getRequestURI();
        if (StrUtil.containsAnyIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }
        String ajax = request.getParameter("__ajax");
        if (StrUtil.containsAnyIgnoreCase(ajax, "json", "xml")) {
            return true;
        }
        return false;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response
     *         渲染对象
     * @param string
     *         待渲染的字符串
     * @return null
     */
    public static String renderString(HttpServletResponse response, String string) throws IOException {
        return renderString(response, string, null);
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response
     *         渲染对象
     * @param string
     *         待渲染的字符串
     * @return null
     */
    public static String renderString(HttpServletResponse response, String string, String type) throws IOException {
        // 注释掉，否则以前设置的Header会被清理掉，如ajax登录设置记住我Cookie
        // response.reset();
        response.setContentType(type == null ? "application/json" : type);
        response.setCharacterEncoding("utf-8");
        response.getWriter().print(string);
        return null;
    }
}
