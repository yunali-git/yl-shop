package com.mr.controller;

import com.mr.entity.StudentEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

/**
 * @ClassName TestController
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/9/15
 * @Version V1.0
 **/
@Controller
public class TestController {

    @GetMapping(value = "test")
    public String test(ModelMap map){
        map.put("name","yiyiyi");
        return "test";
    }

    @GetMapping("student")
    public String student(ModelMap map){
        StudentEntity student=new StudentEntity();
        student.setCode("零七一");
        student.setPass("9527");
        student.setAge(18);
        student.setLikeColor("<font color='red'>红色</font>");
        map.put("stu",student);
        return "student";
    }

    @GetMapping("list")
    public String list(ModelMap map){
        StudentEntity s1=new StudentEntity("001","111",18,"red");
        StudentEntity s2=new StudentEntity("002","222",19,"red");
        StudentEntity s3=new StudentEntity("003","333",16,"blue");
        StudentEntity s4=new StudentEntity("004","444",28,"blue");
        StudentEntity s5=new StudentEntity("005","555",68,"blue");

        map.put("stuList", Arrays.asList(s1,s2,s3,s4,s5));
        return "list";
    }

}
