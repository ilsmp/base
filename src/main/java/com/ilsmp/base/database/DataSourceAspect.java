package com.ilsmp.base.database;

import java.lang.reflect.Method;

import com.ilsmp.base.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Author: zhangjiahao04 Title: DataSourceAspect Package: com.data.export.tool.database Description: 多数据源AOP切面配置 Date:
 * 2022/4/6 17:45
 */
@Aspect
@Configuration
@AutoConfigureAfter(DynamicConfig.class)
public class DataSourceAspect implements Ordered {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Pointcut("@annotation(com.ilsmp.base.database.DataSource)")
    public void dataSourcePointCut() {
    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DataSource ds = method.getDeclaredAnnotation(DataSource.class);
        if (StringUtil.isEmpty(ds.value())) {
            DynamicDataSource.setDataSource(MyCallBack.FIRST);
            log.debug("===datasource is===" + MyCallBack.FIRST);
        } else {
            DynamicDataSource.setDataSource(ds.value());
            log.debug("===datasource is===" + ds.value());
        }
        try {
            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSource();
            log.debug("clean datasource");
        }
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat analogous to Servlet {@code load-on-startup}
     * values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return 1;
    }

}
