package com.mr.entity;

/**
 * @ClassName StudentEntity
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/15
 * @Version V1.0
 **/
public class StudentEntity {
//    private String code;
//    private String pass;
//    private Integer age;
//    private String likeColor;

    String code;
    String pass;
    int age;
    String likeColor;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLikeColor() {
        return likeColor;
    }

    public void setLikeColor(String likeColor) {
        this.likeColor = likeColor;
    }

    public StudentEntity(String code, String pass, int age, String likeColor) {

        this.code = code;
        this.pass = pass;
        this.age = age;
        this.likeColor = likeColor;
    }

    public StudentEntity() {

    }
}
