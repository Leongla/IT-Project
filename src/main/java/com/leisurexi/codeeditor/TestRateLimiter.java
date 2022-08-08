package com.leisurexi.codeeditor;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hj
 * @date 2022/8/1
 */
@SuppressWarnings("all")
public class TestRateLimiter {
    //记录上一次的执行时间
    static long prev = System.nanoTime();

    public static void main(String[] args) {
        //限流器流速：2个请求/秒
        RateLimiter limiter = RateLimiter.create(2.0);

        //执行任务的线程池
        ExecutorService es = Executors.newFixedThreadPool(1);

        //测试执行20次
        for (int i = 0; i < 20; i++) {
            //限流器限流
            limiter.acquire();

            //提交任务异步执行
            es.execute(() -> {
                long cur = System.nanoTime();

                //打印时间间隔
                System.out.println((cur - prev) / 1000_000);
                prev = cur;
            });
        }
    }
}
