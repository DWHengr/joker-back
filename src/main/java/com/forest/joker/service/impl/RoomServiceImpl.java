package com.forest.joker.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forest.joker.entity.Room;
import com.forest.joker.entity.UserRoom;
import com.forest.joker.exception.JokerAopException;
import com.forest.joker.mapper.RoomMapper;
import com.forest.joker.service.RoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forest.joker.service.UserRoomService;
import com.forest.joker.utils.JwtUtil;
import com.forest.joker.utils.RandomUtil;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.CreateRoomVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Service
@Slf4j
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Resource
    UserRoomService userRoomService;

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public JSONObject createRoom(String userid, CreateRoomVo createRoomVo) {
        //用户退出之前房间
        userRoomService.quitRoom(userid);

        //创建房间
        Room room = new Room();
        room.setId(RandomUtil.generateUuid());
        room.setNumber(RandomUtil.generate8NumberByUuid());
        String roomName = createRoomVo.getRoomName();
        if (StringUtils.isEmpty(roomName))
            roomName = "未命名";
        room.setName(StringUtils.substring(roomName, 0, 10));
        room.setType(createRoomVo.getRoomType());
        room.setPassword(createRoomVo.getRoomPassword());
        room.setCreateTime(System.currentTimeMillis());
        room.setUpdateTime(System.currentTimeMillis());
        save(room);

        //用户加入房间
        userRoomService.joinRoom(userid, room.getId(), 1, 1);

        //生成ws房间token
        return ResultUtil.Succeed(createWsTokenInfo(userid, room));
    }

    @Override
    public JSONObject createWsTokenInfo(String userId, Room room) {
        JSONObject info = new JSONObject();
        info.put("roomId", room.getId());
        info.put("roomNumber", room.getNumber());
        info.put("userId", userId);
        info.put("wsToken", JwtUtil.createToken(info));
        return info;
    }

    @Override
    public Object roomInfoByUserid(String userid) {
        UserRoom personalUserRoomInfo = userRoomService.getPersonalUserRoomInfo(userid);
        if (null == personalUserRoomInfo)
            return ResultUtil.Succeed(null);
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getId, personalUserRoomInfo.getRoomId());
        Room room = getOne(wrapper);
        return ResultUtil.Succeed(room);
    }

    @Override
    public Room getRoomInfoByNumber(String roomNumber) {
        try {
            LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Room::getNumber, roomNumber);
            Room room = getOne(wrapper);
            return room;
        } catch (Exception e) {
            throw new JokerAopException("房间信息错误");
        }
    }
}
