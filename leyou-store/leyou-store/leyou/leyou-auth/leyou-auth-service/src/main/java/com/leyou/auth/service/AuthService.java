package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.common.pojo.UserInfo;
import com.leyou.auth.common.utils.JwtUtils;
import com.leyou.auth.config.JwtProperties;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;

    /**
     * 登陆校验 返回token
     *
     * @param username
     * @param password
     * @return
     */
    public String authentication(String username, String password) {
        //调用微服务 执行查询
        User user = userClient.login(username, password);
        //没有结果 返回null
        if (user == null) {
            return null;
        }
        //有结果生成token
        try {
            return JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()), properties.getPrivateKey(), properties.getExpire()*60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
