package com.forest.joker.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forest.joker.constant.WsMsgType;
import com.forest.joker.entity.Room;
import com.forest.joker.entity.User;
import com.forest.joker.entity.UserRoom;
import com.forest.joker.exception.JokerAopException;
import com.forest.joker.mapper.UserRoomMapper;
import com.forest.joker.service.RoomService;
import com.forest.joker.service.UserRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forest.joker.service.UserService;
import com.forest.joker.utils.JwtUtil;
import com.forest.joker.utils.RandomUtil;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.UserJoinRoomVo;
import com.forest.joker.vo.UserQuitRoomVo;
import com.forest.joker.vo.UserRoomInfosVo;
import com.forest.joker.ws.WebSocketService;
import com.forest.joker.ws.WsMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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
    public UserRoom getPersonalUserRoomInfo(String userid) {
        //获取当前用户房间id
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        return userRoom;

    }

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

    @Override
    public boolean quitRoom(String userid) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);
        return remove(userRoomLambdaQueryWrapper);
    }

    @Override
    public boolean joinRoom(String userid, String roomId, int dealers, int owner) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid).eq(UserRoom::getRoomId, roomId);
        int count = count(userRoomLambdaQueryWrapper);
        if (count > 0)
            return true;
        UserRoom userRoom = new UserRoom();
        userRoom.setId(RandomUtil.generateUuid());
        userRoom.setRoomId(roomId);
        userRoom.setUserId(userid);
        userRoom.setScore(0);
        userRoom.setBeforeRoundScore(0);
        userRoom.setIsDealers(dealers);
        userRoom.setIsOwner(owner);
        userRoom.setCreateTime(System.currentTimeMillis());
        userRoom.setUpdateTime(System.currentTimeMillis());
        return save(userRoom);
    }

    @Override
    public JSONObject userJoinRoom(String userid, UserJoinRoomVo userJoinRoomVo) {
        //验证房间密码
        Room room = roomService.getRoomInfoByNumber(userJoinRoomVo.getRoomNumber());
        if (null == room) {
            throw new JokerAopException("房间不存在~").param("roomId", userJoinRoomVo.getRoomNumber());
        }
        if (!room.getPassword().equals(userJoinRoomVo.getRoomPassword())) {
            throw new JokerAopException("房间密码错误~").param("roomId", userJoinRoomVo.getRoomNumber());
        }
        boolean flag = joinRoom(userid, room.getId(), 0, 0);
        WebSocketService.sendAllMessage(room.getId(), new WsMsg(WsMsgType.Info, getUserRoomInfo(userid)));
        if (flag)
            return ResultUtil.Succeed(roomService.createWsTokenInfo(userid, room));
        else
            return ResultUtil.Fail();
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public JSONObject userQuitRoom(String userid, UserQuitRoomVo userQuitRoomVo) {
        //根据用户id和房间id，获取用户房间信息
        UserRoom userRoom = getUserRoomByUserIdAndRoomId(userid, userQuitRoomVo.getRoomId());
        if (null == userRoom)
            return ResultUtil.Succeed();
        boolean flag = false;
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (userRoom.getIsOwner() == 1) {
            //用户为当前房间房主时，解散房间
            userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, userRoom.getRoomId());
            roomService.removeById(userRoom.getRoomId());
            flag = remove(userRoomLambdaQueryWrapper);
            //发送解散房间消息
            WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Quit, null));
            WebSocketService.dissolveRoom(userRoom.getRoomId());
        } else {
            userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, userRoom.getRoomId())
                    .eq(UserRoom::getUserId, userid);
            //退出房间
            flag = remove(userRoomLambdaQueryWrapper);
            WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userRoom.getRoomId())));
            WebSocketService.quitRoom(userRoom.getRoomId(), userid);
        }
        if (flag)
            return ResultUtil.Succeed();
        else
            return ResultUtil.Fail();
    }

    @Override
    public JSONObject createQrToken(String userid) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        if (null == userRoom) {
            return ResultUtil.Fail("房间不存在~");
        }
        Room room = roomService.getById(userRoom.getRoomId());
        if (null == room) {
            return ResultUtil.Fail("房间不存在~");
        }
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomNumber", room.getNumber());
        info.put("roomPassword", room.getPassword());
        String token = JwtUtil.createToken(info);
        JSONObject resultJson = new JSONObject();
        resultJson.put("qrToken", token);
        return ResultUtil.Succeed(resultJson);
    }

    public UserRoom getUserRoomByUserIdAndRoomId(String userId, String roomId) {
        LambdaQueryWrapper<UserRoom> roomLambdaQueryWrapper = new LambdaQueryWrapper();
        roomLambdaQueryWrapper.eq(UserRoom::getRoomId, roomId).eq(UserRoom::getUserId, userId);
        UserRoom userRoom = getOne(roomLambdaQueryWrapper);
        return userRoom;
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

        String roomOwnerUserId = "";
        for (UserRoom userRoom : userRooms) {
            UserRoomInfosVo.UserRoomInfoVo userRoomInfoVo = new UserRoomInfosVo.UserRoomInfoVo();
            BeanUtils.copyProperties(userRoom, userRoomInfoVo);
            //设置用户信息
            User user = userService.getById(userRoom.getUserId());
            userRoomInfoVo.setUsername(user.getName());
//            userRoomInfoVo.setPortrait(user.getPortrait());
            userRoomInfoList.add(userRoomInfoVo);
            if (userRoom.getIsOwner() == 1) {
                roomOwnerUserId = userRoom.getUserId();
            }
        }
        userRoomInfosVo.setRoomOwnerUserId(roomOwnerUserId);
        userRoomInfosVo.setUserRooms(userRoomInfoList);
        return userRoomInfosVo;
    }
}
