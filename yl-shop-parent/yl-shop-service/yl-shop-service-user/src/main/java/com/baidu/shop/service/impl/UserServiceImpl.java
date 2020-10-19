package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.UserConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author yuanli
 * @Date 2020/10/13
 * @Version V1.0
 **/
@Slf4j
@RestController
public class UserServiceImpl extends BaseApiService implements UserService {
    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {
        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));
        userEntity.setCreated(new Date());

        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<UserEntity>> checkUserNameOrPhone(String value, Integer type) {
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(type != null && value != null){
            if(type == UserConstant.USER_TYPE_USERNAME){
                criteria.andEqualTo("username",value);
            }else if(type == UserConstant.USER_TYPE_PHONE){
                criteria.andEqualTo("phone",value);
            }
        }
        List<UserEntity> userEntities = userMapper.selectByExample(example);

        return this.setResultSuccess(userEntities);
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {
        //生成6位随机数 验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";

        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);

        redisRepository.set(UserConstant.VALID_PHONE_CODE + userDTO.getPhone(), code);
        redisRepository.expire(UserConstant.VALID_PHONE_CODE + userDTO.getPhone(),120);

//        LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code); //发送短信验证
//        LuosimaoDuanxinUtil.sendSpeak(userDTO.getPhone(),code); //发送语音验证

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkCode(String phone, String code) {
        String s = redisRepository.get(UserConstant.VALID_PHONE_CODE + phone);
        if(!code.equals(s)) return this.setResultError(HTTPStatus.VALID_CODE_ERROR,"验证码错误");
        return this.setResultSuccess();
    }
}
