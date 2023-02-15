package com.ilsmp.base.database;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Author: zhangjiahao04 Title: DynamicConfig Package: com.data.export.tool.database Description: 动态数据库配置 Date: 2022/4/6
 * 17:26 使用必须要将DataSourceAutoConfiguration给禁止掉：@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
 * 同一个函数中使用多种数据库需在每个库中开启新的事物@Transactional(propagation = Propagation.REQUIRES_NEW)
 */

@Configuration
@ConditionalOnExpression("${spring.base.multi-db:true}")
public class DynamicConfig {

    @Bean(name = MyCallBack.FIRST)
    @ConfigurationProperties(MyCallBack.FIRST_PREFIX)
    public DataSource firstDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = MyCallBack.SECOND)
    @ConfigurationProperties(MyCallBack.SECOND_PREFIX)
    public DataSource secondDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(
            @Qualifier(MyCallBack.FIRST) DataSource firstDataSource,
            @Qualifier(MyCallBack.SECOND) DataSource secondDataSource) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(MyCallBack.FIRST, firstDataSource);
        targetDataSources.put(MyCallBack.SECOND, secondDataSource);
        return new DynamicDataSource(firstDataSource, targetDataSources);
    }

}
