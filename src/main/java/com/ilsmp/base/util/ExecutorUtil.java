package com.ilsmp.base.util;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;

/*
 * Author: zhangjiahao04
 * Description: 线程池工具
 * Date: 2022/11/8 20:37
 * Param:
 * return:
 **/
@Slf4j
public class ExecutorUtil {
    private volatile static ExecutorUtil instance;
    private static final int CORE_SIZE = (int) (Runtime.getRuntime().availableProcessors()
            * 0.25 * (1 + (800 / 200)));
    private static final int MAX_SIZE = (int) (Runtime.getRuntime().availableProcessors()
            * 0.75 * (1 + (800 / 200)));
    private final DefaultManagedAwareThreadFactory threadFactory;
    private final ConcurrentHashMap<String, ThreadPoolExecutor> currentMap = new ConcurrentHashMap<>();

    public static ExecutorUtil getInstance() {
        if (instance == null) {
            synchronized (ExecutorUtil.class) {
                if (instance == null) {
                    instance = new ExecutorUtil();
                }
            }
        }
        return instance;
    }

    private ExecutorUtil() {
        threadFactory = new DefaultManagedAwareThreadFactory();
        threadFactory.setDaemon(true);
        threadFactory.setThreadNamePrefix("schedule-pool-");
    }

    public ThreadPoolExecutor newExecutor(String name) {
        return newExecutor(CORE_SIZE, name);
    }

    public ThreadPoolExecutor newExecutor(int corePoolSize, String name) {
        return newExecutor(corePoolSize, MAX_SIZE, name);
    }

    public ThreadPoolExecutor newFixExecutor(int corePoolSize, String name) {
        return newExecutor(corePoolSize, corePoolSize, name);
    }

    public ThreadPoolExecutor newSingleExecutor(String name) {
        return newExecutor(1, 1, name);
    }

    public ThreadPoolExecutor newExecutor(int corePoolSize, int maxPoolSize, String name) {
        ThreadPoolExecutor executor = currentMap.get(name);
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
        }
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, corePoolSize, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
        currentMap.put(name, executor);
        return executor;
    }

    public ScheduledExecutorService newScheduledExecutor(String name) {
        return newScheduledExecutor(CORE_SIZE, name);
    }

    public ScheduledExecutorService newScheduledExecutor(int corePoolSize, String name) {
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)currentMap.get(name + "scheduled");
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
        }
        executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        currentMap.put(name + "scheduled", executor);
        return executor;
    }

    public ExecutorService getExecutor(String name) {
        return getExecutor(CORE_SIZE, name);
    }

    public ExecutorService getExecutor(int corePoolSize, String name) {
        return getExecutor(corePoolSize, MAX_SIZE, name);
    }

    public ExecutorService getFixedExecutor(String name) {
        return getExecutor(CORE_SIZE, CORE_SIZE, name);
    }

    public ExecutorService getFixedExecutor(int fixedPoolSize, String name) {
        return getExecutor(fixedPoolSize, fixedPoolSize, name);
    }

    public ExecutorService getSingleExecutor(String name) {
        return getExecutor(1, 1, name);
    }

    public ThreadPoolExecutor getExecutor(int corePoolSize, int maxPoolSize, String name) {
        ThreadPoolExecutor executor = currentMap.get(name);
        if (executor != null) {
            return executor;
        }
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, corePoolSize, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
        currentMap.put(name, executor);
        return executor;
    }

    public ScheduledExecutorService getScheduledExecutor(String name) {
        return getScheduledExecutor(CORE_SIZE, name);
    }

    public ScheduledExecutorService getScheduledExecutor(int corePoolSize, String name) {
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)currentMap.get(name + "scheduled");
        if (executor != null) {
            return executor;
        }
        executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        currentMap.put(name + "scheduled", executor);
        return executor;
    }

    public void shutdownExecutor(String name) {
        ThreadPoolExecutor executor = currentMap.get(name);
        if (executor == null) {
            executor = currentMap.get(name + "scheduled");
            if (executor == null) {
                return;
            }
        }
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        currentMap.remove(name);
    }

    public void shutdownAll() {
        Iterator<String> iterator = currentMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            ThreadPoolExecutor executor = currentMap.get(key);
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
            iterator.remove();
        }
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.error("Thread is interrupted.", e);
            Thread.currentThread().interrupt();
        }
    }

}