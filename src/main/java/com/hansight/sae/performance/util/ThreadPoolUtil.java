package com.hansight.sae.performance.util;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ThreadPoolUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtil.class);

    public static ThreadPoolExecutor createPool(int coreThreadNum, String name){
        return new ThreadPoolExecutor(coreThreadNum, coreThreadNum, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(128),
                new BasicThreadFactory.Builder().namingPattern("pool-"+ name +"-%d").daemon(true).build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolExecutor createFixedPool(int coreThreadNum, String name){
        return new ThreadPoolExecutor(coreThreadNum, coreThreadNum, 0, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new BasicThreadFactory.Builder().namingPattern("pool-"+ name +"-%d").daemon(true).build());
    }

    public static ScheduledExecutorService createSchedulePool(int coreThreadNum, String name){
        return new ScheduledThreadPoolExecutor(coreThreadNum,
                new BasicThreadFactory.Builder().namingPattern("pool-"+ name +"-%d").daemon(true).build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolExecutor createTemporaryPool(int coreThreadNum, String name){
        return new ThreadPoolExecutor(coreThreadNum, coreThreadNum, 0, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(coreThreadNum),
                new BasicThreadFactory.Builder().namingPattern("pool-"+ name +"-%d").daemon(true).build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolExecutor createCachePool(String name){
        return new ThreadPoolExecutor(0, 1024, 60, TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                new BasicThreadFactory.Builder().namingPattern("pool-" + name + "-%d").daemon(true).build(),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        logger.error("too many thread created!!!");
                    }
                });
    }
}
