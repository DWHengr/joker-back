package com.forest.joker.service;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forest.joker.vo.LoginVo;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
public interface UserService extends IService<User> {

    JSONObject validateLogin(LoginVo loginVo);
}
