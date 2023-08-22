package com.forest.joker.controller;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.annotation.UrlFree;
import com.forest.joker.service.UserService;
import com.forest.joker.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: dwh
 **/
@RestController
@RequestMapping("/api/login")
@Slf4j
public class LoginController {

    @Resource
    UserService userService;

    @UrlFree
    @PostMapping()
    public Object login(@RequestBody LoginVo loginVo) {
        JSONObject result = userService.validateLogin(loginVo);
        return result;
    }
}
