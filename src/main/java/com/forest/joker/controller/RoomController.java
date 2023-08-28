package com.forest.joker.controller;

import com.forest.joker.annotation.Userid;
import com.forest.joker.service.RoomService;
import com.forest.joker.vo.CreateRoomVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author: dwh
 **/
@RestController
@RequestMapping("/api/room")
@Slf4j
public class RoomController {

    @Resource
    RoomService roomService;

    /**
     * 创建房间
     */
    @PostMapping("/create")
    public Object createRoom(@Userid String userid, @RequestBody CreateRoomVo createRoomVo) {
        return roomService.CreateRoom(userid, createRoomVo);
    }

}
