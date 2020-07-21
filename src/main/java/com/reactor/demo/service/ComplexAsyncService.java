package com.reactor.demo.service;

import com.reactor.demo.model.ClassInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;

/**
 * @author jiaosong
 * @desc
 * @date 2020/7/16
 */
@Slf4j
public class ComplexAsyncService {

    public static Mono<Optional<Integer>> getClassAttend(Integer classId) {
        return Mono.fromSupplier(() -> {
            if (1 == 1) {
                throw new RuntimeException("test");
            }
            return Optional.of(classId + 1);
        }).doOnError(e -> log.error("get class attend fail", e))
                .onErrorReturn(Optional.ofNullable(0))
                .subscribeOn(Schedulers.elastic());
    }

    public static Mono<String> getClassName(Integer classId) {
        return Mono.fromSupplier(() -> {
            return "班级" + classId;
        }).doOnError(e -> log.error("get class name fail", e))
                .onErrorReturn(null)
                .subscribeOn(Schedulers.elastic());
    }

    public static void main(String[] args) {
//        List<ClassInfo> classInfos = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            ClassInfo classInfo = new ClassInfo(i);
//            classInfos.add(classInfo);
//        }
//        Mono<List<ClassInfo>> listMono = Flux.fromIterable(classInfos).flatMap(classInfo -> {
//            return getClassAttend(classInfo.getClassId())
//                    .map(optional -> {
////                        if(optional.isPresent()){
////                            classInfo.setStudentTotal(optional.get());
////                        }
//                        optional.ifPresent(attend -> {
//                            classInfo.setStudentTotal(attend);
//                        });
//                        return classInfo;
//                    });
//        }).collectList();
//        Mono<List<ClassInfo>> listMono1 = Flux.fromIterable(classInfos).flatMap(classInfo -> {
//            return getClassName(classInfo.getClassId())
//                    .map(className -> {
//                        classInfo.setClassName(className);
//                        return classInfo;
//                    });
//        }).collectList();
//        Mono.zip(listMono, listMono1).block();
//        System.out.println(classInfos);
        int i = diffDatePoor(new Date(1595001600000L), initDateByDay(new Date(1595030400000L)), DIFF_DAY);
        System.out.println(i);
    }



    public final static String DIFF_DAY="day";
    public final static String DIFF_HOUR="hour";
    public final static String DIFF_MIN="min";
    public final static String DIFF_SEC="sec";

    /**
     * 计算时间差
     * @param diffdate
     * @param nowDate
     * @return
     */
    public static int diffDatePoor(Date diffdate, Date nowDate, String pattern) {
        if(Objects.isNull(diffdate) || Objects.isNull(nowDate)){
            return 0;
        }
        try {
            long nd = 1000 * 24 * 60 * 60;
            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            long ns = 1000;
            // long ns = 1000;
            // 获得两个时间的毫秒时间差异
            long diff = diffdate.getTime() - nowDate.getTime();
            diff = new BigDecimal(diff).abs().longValue();
            // 计算差多少天
            long day = diff / nd;
            // 计算差多少小时
            long hour = diff % nd / nh;
            // 计算差多少分钟
            long min = diff % nd % nh / nm;
//             计算差多少秒//输出结果
            long sec = diff % nd % nh % nm / ns;
            long result;
            switch (pattern){
                case DIFF_DAY:
                    result =  day;
                    break;
                case DIFF_HOUR:
                    result =  hour;
                    break;
                case DIFF_MIN:
                    result =  min;
                    break;
                default:
                    result = sec;
                    break;
            }
            return (int) result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获得指定日期的零时零分零秒
     * @return
     */
    public static Date initDateByDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }
}
