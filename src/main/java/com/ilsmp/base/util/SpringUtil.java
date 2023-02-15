package com.ilsmp.base.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/*
 * Author: zhangjiahao04
 * Description: spring工具
 * Date 2020/8/5 14:30
 * Param:
 * return:
 **/

/**
 * 在启动类上加入:@Import(SpringUtil.class) 否则系统不会自动调用setApplicationContext方法为工具了赋值
 */
@Slf4j
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 获取applicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
    }

    /**
     * 通过name获取Bean
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /*
     * Author: zhangjiahao04
     * Description: 通过name,以及Clazz刷新容器bean
     * Date: 2022/10/26 18:16
     * Param:
     * return:
     **/
    @SneakyThrows
    public static <T> void refreshBean(String name, Class<T> clazz) {
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
        // 销毁指定实例 name是上文注解过的实例名称
        defaultListableBeanFactory.destroySingleton(name);
        // 按照旧有的逻辑重新获取实例
        T bean = clazz.getConstructor().newInstance();
        // 重新注册同名实例，这样在其他地方注入的实例还是同一个名称，但是实例内容已经重新加载
        defaultListableBeanFactory.registerSingleton(name, bean);
    }

    /*
     * Author: zhangjiahao04
     * Description: 刷新容器所有bean
     * Date: 2022/10/26 18:16
     * Param:
     * return:
     **/
    public static void refreshAll() {
        AnnotationConfigApplicationContext context =
                (AnnotationConfigApplicationContext)applicationContext;
        context.refresh();
    }
}
