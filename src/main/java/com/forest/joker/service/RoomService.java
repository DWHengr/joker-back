package com.forest.joker.service;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forest.joker.vo.CreateRoomVo;

/**
 * <p>
 * 房间表 服务类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
public interface RoomService extends IService<Room> {

    JSONObject CreateRoom(String userid, CreateRoomVo createRoomVo);
}
