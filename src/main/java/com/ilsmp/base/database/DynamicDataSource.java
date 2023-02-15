package com.ilsmp.base.database;

import javax.sql.DataSource;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Author: zhangjiahao04 Title: DynamicDataSource Package: com.data.export.tool.database Description: 动态数据库 Date:
 * 2022/4/6 17:20
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSource = getDataSource();
        if (dataSource != null) {
            log.debug("===DateBase:" + dataSource);
        } else {
            log.debug("===DateBase:" + MyCallBack.FIRST);
        }
        return dataSource;
    }

    public static void setDataSource(String dataSource) {
        HOLDER.set(dataSource);
    }

    public static String getDataSource() {
        return HOLDER.get();
    }

    public static void clearDataSource() {
        HOLDER.remove();
    }

}
