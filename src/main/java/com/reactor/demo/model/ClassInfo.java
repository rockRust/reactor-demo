package com.reactor.demo.model;

import lombok.Data;

/**
 * @author jiaosong
 * @desc
 * @date 2020/6/11
 */
@Data
public class ClassInfo {
    private Integer classId;
    private String className;
    private String teacherName;
    private Integer studentTotal;

    public ClassInfo() {
    }

    public ClassInfo(Integer classId) {
        this.classId = classId;
    }
}
