package com.reactor.demo.service;

import com.reactor.demo.model.ClassInfo;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jiaosong
 * @desc
 * @date 2020/6/11
 */
@Slf4j
public class AsyncService {

    static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4,
            Integer.MAX_VALUE, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    public static void fillClassInfo() {
        Flux<ClassInfo> generateFlux = Flux.generate(() -> 1, (state, sink) -> {
            ClassInfo classInfo = new ClassInfo(state);
            sink.next(classInfo);
            if (state >= 5) {
                sink.complete();
            }
            return state + 1;
        });
        long beginTime = System.currentTimeMillis();
        generateFlux
//                .publishOn(Schedulers.newElastic("thread-get-class-name"))
                .publishOn(Schedulers.fromExecutorService(threadPoolExecutor))
                .doOnNext(c -> {
                    c.setClassName(getClassName(c.getClassId()));
                    System.out.println("[get class name] thread name:" + Thread.currentThread().getName());
                })
//                .publishOn(Schedulers.newElastic("thread-get-teacher-name"))
                .publishOn(Schedulers.fromExecutorService(threadPoolExecutor))
                .doOnNext(c -> {
                    c.setTeacherName(getClassTeacherName(c.getClassId()));
                    System.out.println("[get teacher name] thread name:" + Thread.currentThread().getName());
                })
//                .publishOn(Schedulers.newElastic("thread-get-student-total"))
                .publishOn(Schedulers.fromExecutorService(threadPoolExecutor))
                .doOnNext(c -> {
                    c.setStudentTotal(getClassStudentTotal(c.getClassId()));
                    System.out.println("[get student total] thread name:" + Thread.currentThread().getName());
                })
                .doOnNext(c -> {
                    System.out.println("--------" + c + "thread name:" + Thread.currentThread().getName());
                })
                .doOnError(e -> log.error("fill data fail", e))
                .doOnComplete(() -> {
                    System.out.println("cost time:" + (System.currentTimeMillis() - beginTime));
                })
                .subscribeOn(Schedulers.fromExecutorService(threadPoolExecutor))
                .subscribe();


//        Mono mono1 = Mono.fromSupplier(() -> {
//            getClassStudentTotal(1);
//        }).publishOn(Schedulers.elastic())
//                .timeout(Duration.of(1, ))
//                .onErrorReturn();
//        Mono<tu> mono2 = Mono.fromSupplier(() -> {
//            getClassName(1);
//        });
//        Tuple2<Integer, Integer> block = Mono.zip(mono1, mono2).ma.block();

    }


    private static Integer getClassStudentTotal(Integer classId) {
        sleepOneSecond();
        return classId + 1;
    }

    private static String getClassName(Integer classId) {
        sleepOneSecond();
        return classId + "班";
    }

    private static String getClassTeacherName(Integer classId) {
        sleepOneSecond();
        return classId + "班的老师";
    }

    public static void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        fillClassInfo();
    }


}
