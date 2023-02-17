package com.ilsmp.base.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Author: zhangjiahao04 Title: RestTemplateUtil Package: com.data.export.tool.util Description: http工具类 Date:
 * 2021/10/14 18:31
 */
@Slf4j
public class RestTemplateUtil {

    private static final RestTemplate restTemplate;
    private static final ThreadLocal<Integer> tryNum = ThreadLocal.withInitial(() -> 0);

    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        //单位为ms 60s
        factory.setConnectTimeout(60 * 1000);
        factory.setReadTimeout(60 * 1000);
        restTemplate = new RestTemplate(factory);
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        // 支持中文编码
        converters.set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        // 替换json消息转换器
        MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter(JsonUtil.getInstance());
        // 新增text_plain类型,避免遇到响应体是json,而响应头是text/plain类型无法找到适合的解析
        List<MediaType> mediaTypes = new ArrayList<>(jackson.getSupportedMediaTypes());
        mediaTypes.add(MediaType.TEXT_PLAIN);
        jackson.setSupportedMediaTypes(mediaTypes);
        converters.set(6, jackson);
        converters.add(new FormHttpMessageConverter());
    }

    /**
     * 获取RestTemplate实例对象，可自由调用其方法
     * @return RestTemplate实例对象
     */
    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * GET请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> get(String url, Class<T> responseType) {
        ResponseEntity<T> exchange = restTemplate.getForEntity(url, responseType);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            get(url, responseType);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T getEntity(String url, Class<T> responseType) {
        return get(url, responseType).getBody();
    }

    /**
     * GET请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> get(String url, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> exchange = restTemplate.getForEntity(url, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            get(url, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T getEntity(String url, Class<T> responseType, Object... uriVariables) {
        return get(url, responseType, uriVariables).getBody();
    }

    /**
     * GET请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @param uriVariables  URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> get(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        ResponseEntity<T> exchange = restTemplate.getForEntity(url, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            get(url, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T getEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return get(url, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的GET请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> get(String url, Map<String, String> headers, Class<T> responseType, Object... uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return get(url, httpHeaders, responseType, uriVariables);
    }

    public static <T> T getEntity(String url, Map<String, String> headers, Class<T> responseType, Object... uriVariables) {
        return get(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的GET请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> responseType, Object... uriVariables) {
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        return exchange(url, HttpMethod.GET, requestEntity, responseType, uriVariables);
    }

    public static <T> T getEntity(String url, HttpHeaders headers, Class<T> responseType, Object... uriVariables) {
        return get(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的GET请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> get(String url, Map<String, String> headers, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return get(url, httpHeaders, responseType, uriVariables);
    }

    public static <T> T getEntity(String url, Map<String, String> headers, Class<T> responseType, Map<String, ?> uriVariables) {
        return get(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的GET请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        return exchange(url, HttpMethod.GET, requestEntity, responseType, uriVariables);
    }

    public static <T> T getEntity(String url, HttpHeaders headers, Class<T> responseType,
                                  Map<String, ?> uriVariables) {
        return get(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * POST请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @return ResponseEntity
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> post(String url, Class<T> responseType) {
        ResponseEntity<T> exchange = restTemplate.postForEntity(url, HttpEntity.EMPTY, responseType);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            post(url, responseType);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T postEntity(String url, Class<T> responseType) {
        return post(url, responseType).getBody();
    }

    /**
     * POST请求调用方式
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> post(String url, Object requestBody, Class<T> responseType) {
        ResponseEntity<T> exchange = restTemplate.postForEntity(url, requestBody, responseType);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            post(url, requestBody, responseType);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T postEntity(String url, Object requestBody, Class<T> responseType) {
        return post(url, requestBody, responseType).getBody();
    }

    /**
     * POST请求调用方式
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> post(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> exchange = restTemplate.postForEntity(url, requestBody, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            post(url, requestBody, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T postEntity(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return post(url, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * POST请求调用方式
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> post(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        ResponseEntity<T> exchange = restTemplate.postForEntity(url, requestBody, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            post(url, requestBody, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T postEntity(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return post(url, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的POST请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return post(url, httpHeaders, requestBody, responseType, uriVariables);
    }

    public static <T> T postEntity(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return post(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的POST请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> post(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        return post(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T postEntity(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return post(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的POST请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return post(url, httpHeaders, requestBody, responseType, uriVariables);
    }

    public static <T> T postEntity(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return post(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的POST请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> post(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        return post(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T postEntity(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return post(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 自定义请求头和请求体的POST请求调用方式
     * @param url 请求URL
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> post(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            post(url, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T postEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        return post(url, requestEntity, responseType, uriVariables).getBody();
    }

    /**
     * 自定义请求头和请求体的POST请求调用方式
     * @param url 请求URL
     * @param requestEntity  请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> post(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            post(url, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T postEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        return post(url, requestEntity, responseType, uriVariables).getBody();
    }

    // ----------------------------------PUT-------------------------------------------------------

    /**
     * PUT请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, Class<T> responseType, Object... uriVariables) {
        return put(url, HttpEntity.EMPTY, responseType, uriVariables);
    }

    public static <T> T putEntity(String url, Class<T> responseType, Object... uriVariables) {
        return put(url, HttpEntity.EMPTY, responseType, uriVariables).getBody();
    }

    /**
     * PUT请求调用方式
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
        return put(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T putEntity(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return put(url, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * PUT请求调用方式
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
        return put(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T putEntity(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return put(url, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的PUT请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return put(url, httpHeaders, requestBody, responseType, uriVariables);
    }

    public static <T> T putEntity(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return put(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的PUT请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        return put(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T putEntity(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return put(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的PUT请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return put(url, httpHeaders, requestBody, responseType, uriVariables);
    }

    public static <T> T putEntity(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return put(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的PUT请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> put(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        return put(url, requestEntity, responseType, uriVariables);
    }

    public static <T> ResponseEntity<T> putEntity(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return put(url, headers, requestBody, responseType, uriVariables);
    }

    /**
     * 自定义请求头和请求体的PUT请求调用方式
     * @param url 请求URL
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> put(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            put(url, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> ResponseEntity<T> putEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        return put(url, requestEntity, responseType, uriVariables);
    }

    /**
     * 自定义请求头和请求体的PUT请求调用方式
     * @param url 请求URL
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> put(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            put(url, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T putEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        return put(url, requestEntity, responseType, uriVariables).getBody();
    }

    /**
     * DELETE请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Class<T> responseType, Object... uriVariables) {
        return delete(url, HttpEntity.EMPTY, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Class<T> responseType, Object... uriVariables) {
        return delete(url, responseType, uriVariables).getBody();
    }

    /**
     * DELETE请求调用方式
     * @param url 请求URL
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, HttpEntity.EMPTY, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, responseType, uriVariables).getBody();
    }

    /**
     * DELETE请求调用方式
     * @param url 请求URL
     * @param requestBody  请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
        return delete(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return delete(url, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * DELETE请求调用方式
     * @param url 请求URL
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody);
        return delete(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Map<String, String> headers, Class<T> responseType, Object... uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return delete(url, httpHeaders, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Map<String, String> headers, Class<T> responseType, Object... uriVariables) {
        return delete(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, HttpHeaders headers, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        return delete(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, HttpHeaders headers, Class<T> responseType, Object... uriVariables) {
        return delete(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Map<String, String> headers, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return delete(url, httpHeaders, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Map<String, String> headers, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        return delete(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, HttpHeaders headers, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return delete(url, httpHeaders, requestBody, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return delete(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        return delete(url, requestEntity, responseType, uriVariables);
    }

    public static <T> ResponseEntity<T> deleteEntity(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Object... uriVariables) {
        return delete(url, headers, requestBody, responseType, uriVariables);
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAll(headers);
        return delete(url, httpHeaders, requestBody, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, Map<String, String> headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, headers, requestBody, responseType, uriVariables).getBody();
    }

    /**
     * 带请求头的DELETE请求调用方式
     * @param url 请求URL
     * @param headers 请求头参数
     * @param requestBody 请求参数体
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    public static <T> ResponseEntity<T> delete(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String, ?> uriVariables) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
        return delete(url, requestEntity, responseType, uriVariables);
    }

    public static <T> T deleteEntity(String url, HttpHeaders headers, Object requestBody, Class<T> responseType, Map<String
            , ?> uriVariables) {
        return delete(url, headers, responseType, uriVariables).getBody();
    }

    /**
     * 自定义请求头和请求体的DELETE请求调用方式
     * @param url 请求URL
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> delete(String url, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            delete(url, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T deleteEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType,
                                     Object... uriVariables) {
        return delete(url, requestEntity, responseType, uriVariables).getBody();
    }

    /**
     * 自定义请求头和请求体的DELETE请求调用方式
     * @param url 请求URL
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> delete(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            delete(url, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T deleteEntity(String url, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        return delete(url, requestEntity, responseType, uriVariables).getBody();
    }

    /**
     * 通用调用方式
     * @param url 请求URL
     * @param method 请求方法类型
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，按顺序依次对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            exchange(url, method, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T exchangeEntity(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                       Class<T> responseType, Object... uriVariables) {
        return exchange(url, method, requestEntity, responseType, uriVariables).getBody();
    }

    /**
     * 通用调用方式
     * @param url 请求URL
     * @param method 请求方法类型
     * @param requestEntity 请求头和请求体封装对象
     * @param responseType 返回对象类型
     * @param uriVariables URL中的变量，与Map中的key对应
     * @return ResponseEntity 响应对象封装类
     */
    @SneakyThrows
    public static <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
        tryNum.set(tryNum.get()+1);
        log.warn("===第{}次{}:{}", tryNum.get(), url, JsonUtil.writeJsonStr(Objects.requireNonNull(exchange.getBody())));
        if (exchange.getStatusCode().isError() && tryNum.get() < 3) {
            Thread.sleep(tryNum.get()*3000);
            exchange(url, method, requestEntity, responseType, uriVariables);
        }
        tryNum.remove();
        return exchange;
    }

    public static <T> T exchangeEntity(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) {
        return exchange(url, method, requestEntity, responseType, uriVariables).getBody();
    }

}