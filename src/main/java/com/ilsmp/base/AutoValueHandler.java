package com.ilsmp.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ilsmp.base.util.EncryptUtil;
import com.ilsmp.base.util.JsonUtil;
import com.ilsmp.base.util.ServletUtil;
import com.ilsmp.base.util.TimeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * @author: ZJH Title: AutoValueHandler Package com.zhihui.gongdi.tool Description: AutoValuez注解实现帮助类 Date 2020/4/22
 * 17:01 注意：Bean后置处理器本身也是一个Bean
 */
@Configuration
@Aspect
@Slf4j
public class AutoValueHandler {

    private static volatile ThreadLocal<Integer> tryNum = new ThreadLocal<>();

    @Value("${spring.base.user-id:user-id}")
    private String userId;

    @Around("execution(* org.springframework.data.repository.Repository+.update*(..))")
    public Object invoked(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> list = Arrays.asList(joinPoint.getArgs());
        try {
            log.info("入库update拦截参数：{}", JsonUtil.writeJsonStr(list));
        } catch (JsonProcessingException e) {
            log.error("入库update拦截参数", e);
        }
        return activeAutoValue(joinPoint, list, false);
    }

    /**
     * 定义切点（切入位置） //@Pointcut(value = "this(org.springframework.data.jpa.repository.JpaRepository) && execution(*
     * *save*(..))") //@Pointcut(value = "@annotation(com.zhihui.gongdi.tool.AutoValue)")
     */
    @Around("execution(* org.springframework.data.repository.Repository+.save*(..))")
    private Object autoValue(ProceedingJoinPoint joinPoint) {
        List<Object> list = Arrays.asList(joinPoint.getArgs());
        try {
            log.info("入库save拦截参数：{}", JsonUtil.writeJsonStr(list));
        } catch (JsonProcessingException e) {
            log.error("入库save拦截参数", e);
        }
        return activeAutoValue(joinPoint, list, true);
    }

    private Object activeAutoValue(ProceedingJoinPoint joinPoint, List<Object> list, Boolean isSave) {
        list.forEach(arg -> {
            if (arg instanceof List && ((List) arg).size() > 0) {
                long updateVersion = System.currentTimeMillis();
                ((List) arg).forEach(item -> {
                    if (item instanceof BaseEntityVersion) {
                        getAnnotation(item, updateVersion, isSave);
                    }
                });
            } else if (arg instanceof BaseEntityVersion) {
                getAnnotation(arg, System.currentTimeMillis(), isSave);
            }
        });
        Object obj = null;
        try {
            obj = joinPoint.proceed();
        } catch (Throwable throwable) {
            if (tryNum.get() > 2) {
                tryNum.set(0);
                return null;
            } else {
                tryNum.set(tryNum.get()+1);
                log.error("自定义AOP重试{}次异常：{}", tryNum, throwable);
                obj = activeAutoValue(joinPoint, list, isSave);
            }
        }
        return obj;
    }

    @SneakyThrows
    private void getAnnotation(Object bean, long updateVersion, Boolean isSave) {
        /**
         * 利用Java反射机制注入属性
         */
        Class<?> aClass = bean.getClass();
        List<Field> fields = new ArrayList<>(Arrays.asList(aClass.getDeclaredFields()));
        while (!BaseEntityVersion.class.equals(aClass)) {
            aClass = aClass.getSuperclass();
            fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
        }
        for (Field field : fields) {
            field.setAccessible(true);
            if (!isSave) {
                // 更新
                LastModifiedDate lastModifiedDate = field.getAnnotation(LastModifiedDate.class);
                if (lastModifiedDate != null) {
                    fieldSet(field, bean, TimeUtil.obtainCurrentTimestamp());
                }
                LastModifiedBy lastModifiedBy = field.getAnnotation(LastModifiedBy.class);
                if (lastModifiedBy != null) {
                    fieldSet(field, bean, ServletUtil.getRequestObject(userId));
                }
                field.setAccessible(false);
            } else {
                AutoValue annotation = field.getAnnotation(AutoValue.class);
                if (annotation != null) {
                    String fieldName = annotation.value();
                    String property;
                    if ("".equals(fieldName)) {
                        // 设置AutoValue注解字段值
                        fieldSet(field, bean, updateVersion);
                    } else {
                        // 设置AutoValue注解的field的值的crc32编码设置AutoValue注解的value的值对应的field字段值
                        try {
                            property = JsonUtil.writeJsonStr(field.get(bean));
                            if (property == null || property.equals("null")) {
                                return;
                            }
                            Field targetField = bean.getClass().getDeclaredField(fieldName);
                            targetField.setAccessible(true);
                            String targetValue = JsonUtil.writeJsonStr(targetField.get(bean));
                            if (targetValue != null && !"null".equals(targetValue) && tryNum.get() < 2) {
                                return;
                            } else {
                                if (targetValue == null || targetValue.equals("null")) {
                                    targetValue = "";
                                }
                            }
                            long fieldCode = EncryptUtil.crc32FromStr(property + targetValue);
                            field.setAccessible(false);
                            field = targetField;
                            // 设置值
                            fieldSet(field, bean, fieldCode);
                        } catch (Exception e) {
                            log.error("AutoValue注解生成code失败！", e);
                        }
                    }
                }
            }

        }
    }

    private void fieldSet(Field field, Object bean, Object fieldValue) {
        try {
            if (field.getType().equals(String.class)) {
                field.set(bean, String.valueOf(fieldValue));
            } else {
                field.set(bean, fieldValue);
            }
        } catch (IllegalAccessException e) {
            log.error("AutoValue设置属性值失败！", e);
        } finally {
            field.setAccessible(false);
        }
    }

}
