package com.forest.joker.service.impl;

import com.forest.joker.entity.User;
import com.forest.joker.mapper.UserMapper;
import com.forest.joker.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
