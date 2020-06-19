package com.reactor.demo.service;

import com.reactor.demo.model.ClassInfo;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
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


    public static ClassInfo fillClassInfo() {
//        Flux<ClassInfo> generateFlux = Flux.generate(() -> 1, (state, sink) -> {
//            ClassInfo classInfo = new ClassInfo(state);
//            sink.next(classInfo);
//            if (state >= 5) {
//                sink.complete();
//            }
//            return state + 1;
//        });
//        long beginTime = System.currentTimeMillis();
//        generateFlux
////                .publishOn(Schedulers.newElastic("thread-get-class-name"))
//                .publishOn(Schedulers.fromExecutorService(threadPoolExecutor))
//                .doOnNext(c -> {
//                    c.setClassName(getClassName(c.getClassId()));
//                    System.out.println("[get class name] thread name:" + Thread.currentThread().getName());
//                })
////                .publishOn(Schedulers.newElastic("thread-get-teacher-name"))
//                .publishOn(Schedulers.fromExecutorService(threadPoolExecutor))
//                .doOnNext(c -> {
//                    c.setTeacherName(getClassTeacherName(c.getClassId()));
//                    System.out.println("[get teacher name] thread name:" + Thread.currentThread().getName());
//                })
////                .publishOn(Schedulers.newElastic("thread-get-student-total"))
//                .publishOn(Schedulers.fromExecutorService(threadPoolExecutor))
//                .doOnNext(c -> {
//                    c.setStudentTotal(getClassStudentTotal(c.getClassId()));
//                    System.out.println("[get student total] thread name:" + Thread.currentThread().getName());
//                })
//                .doOnNext(c -> {
//                    System.out.println("--------" + c + "thread name:" + Thread.currentThread().getName());
//                })
//                .doOnError(e -> log.error("fill data fail", e))
//                .doOnComplete(() -> {
//                    System.out.println("cost time:" + (System.currentTimeMillis() - beginTime));
//                })
//                .subscribeOn(Schedulers.fromExecutorService(threadPoolExecutor))
//                .subscribe();


//        Mono mono1 = Mono.fromSupplier(() -> {
//            ClassInfo classInfo = new ClassInfo(1);
//            getClassStudentTotal(1);
//            return classInfo;
//        }).publishOn(Schedulers.elastic())
//                .onErrorReturn(null);
//        Mono.fromSupplier((c) -> {
//            getClassName(1);
//        });
//        Tuple2<Integer, Integer> block = Mono.zip(mono1, mono2).ma.block();
        Mono<ClassInfo> classInfoMono = Mono.fromSupplier(() -> {
            return new ClassInfo(1);
        });
        Mono<Integer> studentTotalMono = Mono.fromSupplier(() -> getClassStudentTotal(1)).subscribeOn(Schedulers.elastic());
        Mono<String> classNameMono = Mono.fromSupplier(() -> getClassName(1)).subscribeOn(Schedulers.elastic());
        Mono<String> teacherNameMono = Mono.fromSupplier(() -> getClassTeacherName(1)).subscribeOn(Schedulers.elastic());

        ClassInfo block = Mono.zip(studentTotalMono, classNameMono, teacherNameMono)
                .subscribeOn(Schedulers.elastic())
                .map(tuple3 -> {
                    ClassInfo classInfo = new ClassInfo(1);
                    classInfo.setStudentTotal(tuple3.getT1());
                    classInfo.setClassName(tuple3.getT2());
                    classInfo.setTeacherName(tuple3.getT3());
                    return classInfo;
                })
                .block();

        return block;
    }

    public static List<ClassInfo> getClassInfos() {
        List<ClassInfo> classInfos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ClassInfo classInfo = new ClassInfo(i);
            classInfos.add(classInfo);
        }

        Mono<List<ClassInfo>> classNameMono = Flux.fromStream(classInfos.stream())
                .flatMap(classInfo -> {
                    return Mono.fromSupplier(() -> {
                        return getClassName(classInfo.getClassId());
                    }).map((className) -> {
                        classInfo.setClassName(className);
                        return classInfo;
                    })      // 真正并发是因为这里订阅到弹性线程了
                            .subscribeOn(Schedulers.elastic());
                }).collectList();

        Mono<List<ClassInfo>> teacherNameMono = Flux.fromStream(classInfos.stream())
                .flatMap(classInfo -> {
                    return Mono.fromSupplier(() -> {
                        return getClassTeacherName(classInfo.getClassId());
                    }).map((teacherName) -> {
                        classInfo.setTeacherName(teacherName);
                        return classInfo;
                    }).subscribeOn(Schedulers.elastic());
                }).collectList();

        Mono<List<ClassInfo>> studentNumMono = Flux.fromStream(classInfos.stream())
                .flatMap(classInfo -> {
                    return Mono.fromSupplier(() -> {
                        return getClassStudentTotal(classInfo.getClassId());
                    }).map((studentNum) -> {
                        classInfo.setStudentTotal(studentNum);
                        return classInfo;
                    }).subscribeOn(Schedulers.elastic());
                }).collectList();
//
        Mono.zip(classNameMono, teacherNameMono, studentNumMono)
//                .subscribeOn(Schedulers.elastic())
                .block();
        return classInfos;
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
        long startTime = System.currentTimeMillis();
//        ClassInfo classInfo = fillClassInfo();
        List<ClassInfo> classInfos = getClassInfos();
        log.info("result:{}", classInfos);
        log.info("cost time:{}", System.currentTimeMillis() - startTime);
    }


}
