package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.Mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    //统一手机号前缀
    private static final String KEY_PREFIX = "user:code:phone:";


    //校验用户是否存在 type=1表示用户名 2表示手机号
    public Boolean validateUserData(String data, Integer type) {
        User record = new User();
        if (type == 1) {
            record.setUsername(data);
        } else if (type == 2) {
            record.setPhone(data);
        }

        //查询不到表示用户不存在可以使用
        return userMapper.selectCount(record) == 0;
    }

    //生成验证码
    public Boolean sendVerifyCode(String phone){

     if(StringUtils.isBlank(phone)){
        return false;
     }
        try {
            //生成验证码
            String code = NumberUtils.generateCode(6);

            //将手机号对应的验证码存入redis 期限为五分钟
            redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5,TimeUnit.MINUTES);


            Map<String,String> msg=new HashMap<>();
            msg.put("phone",phone);
            msg.put("code",code);
            //发送验证码
            amqpTemplate.convertAndSend("LEYOU-SMS-EXCHANGE","sms.verifycode",msg);
            return  true;
        } catch (AmqpException e) {
            e.printStackTrace();
            return false;
        }

    }

    //注册
    public Boolean register(User user,String code){
        // 短信校验 从reids获取验证码
        String redisCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if(!StringUtils.equals(code,redisCode)){
            return false;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加密
       user.setPassword( CodecUtils.md5Hex(user.getPassword(),salt));
       //设置其余的参数
        user.setId(null);
        user.setCreated(new Date());
        //写入数据库
        Boolean haveUser = userMapper.insertSelective(user) == 1;
        //删除redis中的缓存
        if(haveUser){
            //注册成功 删除redis中的数据
            redisTemplate.delete(KEY_PREFIX + user.getPhone());

        }
        return haveUser;
    }

    public User login(String username,String password){
        User record = new User();
        record.setUsername(username);
        //查找是否有此用户名
        User user = userMapper.selectOne(record);
        if(user==null){
            return null;
        }
       //校验密码
        if(!user.getPassword().equals(CodecUtils.md5Hex(password,user.getSalt()))){
            return null;
        }
        //账号密码正确
        return user;
    }
}
