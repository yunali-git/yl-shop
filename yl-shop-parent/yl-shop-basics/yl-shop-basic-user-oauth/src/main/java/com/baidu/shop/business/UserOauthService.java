package com.baidu.shop.business;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.entity.UserEntity;

public interface UserOauthService {
    String login(UserEntity userEntity, JwtConfig jwtConfig);
}
