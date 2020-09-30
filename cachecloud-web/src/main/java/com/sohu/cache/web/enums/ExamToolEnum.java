package com.sohu.cache.web.enums;

/**
 * Created by rucao on 2019/1/29
 */
public enum ExamToolEnum {
    EXAM_ALL(0,"exam_all"),
    EXAM_NON_TEST(1,"exam_non_test"),
    EXAM_LIST(2,"exam_appid_list"),
    EXAM_APPID(3,"exam_appid");

    private final int value;
    private final String examTypeStr;

    ExamToolEnum (int value,String examTypeStr){
        this.value=value;
        this.examTypeStr=examTypeStr;
    }

    public int getValue(){
        return this.value;
    }
    public String getExamTypeStr(){
        return  this.examTypeStr;
    }

}
