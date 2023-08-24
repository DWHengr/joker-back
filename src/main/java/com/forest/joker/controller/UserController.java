package com.forest.joker.controller;

import com.forest.joker.entity.User;
import com.forest.joker.service.UserService;
import com.forest.joker.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: dwh
 **/
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    /**
     * 根据用户id获取用户头像
     */
    @GetMapping("/portrait")
    public Object userRoomInfo(@RequestParam String userid) {
        User user = userService.getById(userid);
        if (null != user)
            return ResultUtil.Succeed(user.getPortrait());
        return ResultUtil.Fail();
    }
}