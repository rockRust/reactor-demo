package com.reactor.demo.service;

import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @author jiaosong
 * @desc
 * @date 2020/6/19
 */
public class SplitService {
    public static Map<Integer, String> nameMap = new HashMap();

    static {
        nameMap.put(1, "一");
        nameMap.put(2, "二");
        nameMap.put(3, "三");
        nameMap.put(4, "四");
    }

    public static void getNames(String departmentIdsString) {
        String block = Flux.fromStream(() -> {
            return Arrays.stream(departmentIdsString.split(","));
        }).flatMap(departmentIdString -> {
            return Mono.fromSupplier(() -> {
                return nameMap.get(Integer.valueOf(departmentIdString));
            });
        }).collectList().map(departmentNameList -> {
            return StringUtils.join(departmentNameList, ",");
        }).block();
        System.out.println(block);
    }

    public static void main(String[] args) {
        getNames("1,3,2,4");
    }
}
