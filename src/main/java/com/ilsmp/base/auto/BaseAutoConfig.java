package com.ilsmp.base.auto;

import javax.annotation.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

/**
 * Author: zhangjiahao04 Title: BaseAutoConfig Package: com.ilsmp.base.tool.auto Description: 自动配置 Date: 2022/5/9 13:25
 */
@Component
@EnableConfigurationProperties({BaseProperties.class})
@ComponentScan(basePackages = {"${spring.base.component-scan:com.*.common}", "com.ilsmp.base"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.*?\\..*?\\..*?\\..feign$"))
public class BaseAutoConfig {
    @Resource
    private BaseProperties baseProperties;
}
