package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.reflect.generics.tree.VoidDescriptor;

import javax.net.ssl.HttpsURLConnection;
import javax.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> validateUserData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        Boolean bool = userService.validateUserData(data, type);
        if (bool == null) {
            //400
            return ResponseEntity.badRequest().build();
        }

        //200
        return ResponseEntity.ok(bool);
    }

    /**
     *  生成验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone) {

        Boolean bool = userService.sendVerifyCode(phone);
        if (bool == null || !bool) {
            //400
            return ResponseEntity.badRequest().build();
        }
        //204
        return new ResponseEntity<>(HttpStatus.CREATED);

    }


    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code) {
        Boolean bool = userService.register(user, code);

        if (bool == null || !bool) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PostMapping("login")
    public ResponseEntity<User> login(@RequestParam("username") String username,@RequestParam("password") String password) {

        User user = userService.login(username, password);
        if(user==null){
            return  ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);

    }



}
