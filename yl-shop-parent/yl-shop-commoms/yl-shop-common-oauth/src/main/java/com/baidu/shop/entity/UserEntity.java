package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName UserEntity
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Table(name = "tb_user")
@Data
public class UserEntity {
    @Id
    private Integer id;

    private String username;

    private String password;

    private String phone;

    private Date created;

    private String salt;
}
