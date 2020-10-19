package com.baidu.shop.business.impl;

import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserOauthServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class UserOauthServiceImpl implements UserOauthService {
    @Resource
    private UserOauthMapper userOauthMapper;

    @Override
    public String login(UserEntity userEntity, JwtConfig jwtConfig) {
        String token = null;

        Example example = new Example(UserEntity.class);

        example.createCriteria().andEqualTo("username",userEntity.getUsername());
        List<UserEntity> list = userOauthMapper.selectByExample(example);

        if(list.size() == 1){
            UserEntity entity = list.get(0);
            //比较密码
            if (BCryptUtil.checkpw(userEntity.getPassword(),entity.getPassword())) {
                //创建token
                try {
                    token = JwtUtils.generateToken(new UserInfo(entity.getId(),entity.getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }

}
