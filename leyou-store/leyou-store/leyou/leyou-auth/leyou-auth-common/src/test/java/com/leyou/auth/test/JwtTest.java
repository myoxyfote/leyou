package com.leyou.auth.test;

import com.leyou.auth.common.pojo.UserInfo;
import com.leyou.auth.common.utils.JwtUtils;
import com.leyou.auth.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "/Users/wentimei/Documents/Tools/Rsa/rsa.pub";

    private static final String priKeyPath = "/Users/wentimei/Documents/Tools/Rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "2s*&32_-`/,/");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTUzNzIzNDg2N30.ZFFk__DokquyGSNB8QqBrprsFTVfZx5mWF6Jzrd1Oi6nqr-y_pUVFrwx1_tMQf01ntS_J7xhMl1cE22TwR6RueH64pGKWYQL6t_ODS-0G-vncekzXI0OUvOdgq0V6WhBWZ4POb3aJ3YR8Zn2Pfam2Xbmxy0z4EZBqYl8NOmbqeM";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}