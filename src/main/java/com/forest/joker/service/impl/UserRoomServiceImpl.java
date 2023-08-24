package com.forest.joker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forest.joker.entity.Room;
import com.forest.joker.entity.User;
import com.forest.joker.entity.UserRoom;
import com.forest.joker.mapper.UserRoomMapper;
import com.forest.joker.service.RoomService;
import com.forest.joker.service.UserRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forest.joker.service.UserService;
import com.forest.joker.vo.UserRoomInfosVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 用户房间表 服务实现类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Service
@Slf4j
public class UserRoomServiceImpl extends ServiceImpl<UserRoomMapper, UserRoom> implements UserRoomService {

    @Resource
    RoomService roomService;

    @Resource
    UserService userService;

    @Override
    public UserRoomInfosVo getUserRoomInfo(String userid) {
        //获取当前用户房间id
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);

        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        if (null != userRoom)
            return getUserRoomInfoByRoomId(userRoom.getRoomId());

        return null;

    }

    /**
     * 根据房间号获取房间的用户信息
     */
    public UserRoomInfosVo getUserRoomInfoByRoomId(String roomId) {
        //房间信息
        Room room = roomService.getById(roomId);
        if (null == room)
            return null;
        UserRoomInfosVo userRoomInfosVo = new UserRoomInfosVo();
        userRoomInfosVo.setRoom(room);

        //房间所有用户信息
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, roomId);
        List<UserRoom> userRooms = list(userRoomLambdaQueryWrapper);
        List<UserRoomInfosVo.UserRoomInfoVo> userRoomInfoList = new ArrayList<>();

        for (UserRoom userRoom : userRooms) {
            UserRoomInfosVo.UserRoomInfoVo userRoomInfoVo = new UserRoomInfosVo.UserRoomInfoVo();
            BeanUtils.copyProperties(userRoom, userRoomInfoVo);
            //设置用户信息
            User user = userService.getById(userRoom.getUserId());
            userRoomInfoVo.setUsername(user.getName());
//            userRoomInfoVo.setPortrait(user.getPortrait());
            userRoomInfoList.add(userRoomInfoVo);
        }
        userRoomInfosVo.setUserRooms(userRoomInfoList);
        return userRoomInfosVo;
    }
}
