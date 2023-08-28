package com.forest.joker.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forest.joker.entity.Room;
import com.forest.joker.entity.User;
import com.forest.joker.entity.UserRoom;
import com.forest.joker.mapper.UserMapper;
import com.forest.joker.service.RoomService;
import com.forest.joker.service.UserRoomService;
import com.forest.joker.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forest.joker.utils.JwtUtil;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.LoginVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

    @Resource
    JwtUtil jwtUtil;

    @Resource
    UserRoomService userRoomService;

    @Resource
    RoomService roomService;

    @Override
    public JSONObject validateLogin(LoginVo loginVo) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(User::getAccount, loginVo.getAccount());
        User user = getOne(queryWrapper);
        if (null == user) {
            return ResultUtil.Fail("用户名错误");
        }
        if (!user.getPassword().equals(loginVo.getPassword())) {
            return ResultUtil.Fail("密码错误");
        }
        JSONObject userinfo = new JSONObject();
        userinfo.put("userId", user.getId());
        try {
            LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, user.getId());
            UserRoom userRoom = userRoomService.getOne(userRoomLambdaQueryWrapper);
            userinfo.put("roomId", userRoom.getRoomId());
            //房间编号
            LambdaQueryWrapper<Room> roomLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomLambdaQueryWrapper.eq(Room::getId, userRoom.getRoomId());
            Room room = roomService.getOne(roomLambdaQueryWrapper);
            userinfo.put("roomNumber", room.getNumber());
        } catch (Exception e) {
            userinfo.put("roomId", null);
            userinfo.put("roomNumber", null);
        }
        //生成ws的token
        userinfo.put("wsToken", jwtUtil.createToken(userinfo));
        userinfo.put("account", user.getAccount());
        userinfo.put("username", user.getName());
        userinfo.put("phone", user.getPhone());
        userinfo.put("email", user.getEmail());

        //生成用户token
        userinfo.put("token", jwtUtil.createToken(userinfo));
        return ResultUtil.Succeed(userinfo);
    }
}
