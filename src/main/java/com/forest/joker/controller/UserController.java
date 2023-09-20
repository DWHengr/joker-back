package com.forest.joker.controller;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.annotation.Userid;
import com.forest.joker.entity.User;
import com.forest.joker.service.UserService;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.ModifyNameVo;
import com.forest.joker.vo.ProfileResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

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
    public Object userPortrait(@RequestParam String userid) {
        User user = userService.getById(userid);
        if (null != user)
            return ResultUtil.Succeed(user.getPortrait());
        return ResultUtil.Fail();
    }

    /**
     * 获取用户个人信息
     */
    @GetMapping("/profile")
    public Object userProfile(@Userid String userid) {
        User user = userService.getById(userid);
        if (null != user) {
            ProfileResultVo profileResultVo = new ProfileResultVo();
            BeanUtils.copyProperties(user, profileResultVo);
            return ResultUtil.Succeed(profileResultVo);
        }
        return ResultUtil.Fail();
    }

    /**
     * 修改用户名称
     */
    @PostMapping("/modify/name")
    public Object modifyName(@Userid String userid, @RequestBody ModifyNameVo modifyNameVo) {
        if (StringUtils.isEmpty(modifyNameVo.getName())) {
            return ResultUtil.Fail("用户名称不能为空~");
        }
        JSONObject result = userService.modifyName(userid, modifyNameVo);
        return result;
    }
}
