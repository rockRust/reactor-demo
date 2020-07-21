package com.reactor.demo.service;

import com.reactor.demo.model.ClassInfo;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

        Mono<List<ClassInfo>> classNameMono = Flux.fromIterable(classInfos)
                .flatMap(classInfo -> {
                    return Mono.fromSupplier(() -> {
                        return getClassName(classInfo.getClassId());
                    }).map((className) -> {
                        classInfo.setClassName(className);
                        return classInfo;
                    })      // 真正并发是因为这里订阅到弹性线程了
                            .subscribeOn(Schedulers.elastic());
                }).collectList();

        Mono<List<ClassInfo>> teacherNameMono = Flux.fromIterable(classInfos)
                .flatMap(classInfo -> {
                    return Mono.fromSupplier(() -> {
                        return getClassTeacherName(classInfo.getClassId());
                    }).map((teacherName) -> {
                        classInfo.setTeacherName(teacherName);
                        return classInfo;
                    }).subscribeOn(Schedulers.elastic());
                }).collectList();

        Mono<List<ClassInfo>> studentNumMono = Flux.fromIterable(classInfos)
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
        log.info("get class name,classId:{}", classId);
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
//        long startTime = System.currentTimeMillis();
////        ClassInfo classInfo = fillClassInfo();
////        List<ClassInfo> classInfos = getClassInfos();
//        Mono.fromSupplier(() -> {
//             return getClassName(1);
//        })
//                .subscribeOn(Schedulers.elastic())
//                .subscribe();
////        log.info("result:{}", classInfos);
//        log.info("cost time:{}", System.currentTimeMillis() - startTime);
        Double percentNum = getPercentNum(3, 200, 3, null);
        String s = convertPercentageHalfUp(percentNum, 0);
        System.out.println(s);


    }

    /**
     * 计算小数 可控制参数
     *
     * @param numerator       分子
     * @param denominator     分母
     * @param digit           保留小数位 如4 保留四位小数
     * @param calculationType 计算类型 BIgDecimal常量 如RoundingMode.HALF_UP/BigDecimal.ROUND_HALF_UP 默认四舍五入
     * @return
     */
    public static Double getPercentNum(Integer numerator, Integer denominator, Integer digit, RoundingMode calculationType) {
        if (Objects.isNull(numerator) || Objects.isNull(denominator) || denominator == 0) {
            return null;
        }
        if (Objects.isNull(calculationType)) {
            calculationType = RoundingMode.HALF_UP;
        }
        return new BigDecimal(numerator).divide(new BigDecimal(denominator), digit, calculationType).doubleValue();
    }

    /**
     * 转换百分比 进行四舍五入
     *
     * @param value 字符串类型，保证精度
     * @param digit 小数位
     * @return
     */
    public static String convertPercentageHalfUp(Object value, Integer digit) {
        if (Objects.isNull(value)) {
            return 0 + "%";
        }
        BigDecimal multiply = new BigDecimal(value.toString()).multiply(new BigDecimal(100)).setScale(digit, BigDecimal.ROUND_HALF_UP);
        return multiply.toString() + "%";
    }


}
