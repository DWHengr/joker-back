package com.forest.joker.controller;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.annotation.Userid;
import com.forest.joker.service.UserRoomService;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.UserJoinRoomVo;
import com.forest.joker.vo.UserQuitRoomVo;
import com.forest.joker.vo.UserRoomInfosVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author: dwh
 **/
@RestController
@RequestMapping("/api/userRoom")
@Slf4j
public class UserRoomController {

    @Resource
    UserRoomService userRoomService;

    /**
     * 获取房间用户信息，通过当前用户
     */
    @GetMapping("/info")
    public Object userRoomInfo(@Userid String userid) {
        UserRoomInfosVo userRoomInfo = userRoomService.getUserRoomInfo(userid);
        return ResultUtil.Succeed(userRoomInfo);
    }

    /**
     * 用户加入房间
     */
    @PostMapping("/join")
    public Object userJoinRoom(@Userid String userid, @RequestBody UserJoinRoomVo userJoinRoomVo) {
        JSONObject result = userRoomService.userJoinRoom(userid, userJoinRoomVo);
        return result;
    }


    /**
     * 房间二维码token生成
     */
    @GetMapping("/qr/token")
    public Object createQrToken(@Userid String userid) {
        JSONObject result = userRoomService.createQrToken(userid);
        return result;
    }

    /**
     * 用户退出房间
     */
    @PostMapping("/quit")
    public Object userQuitRoom(@Userid String userid, @RequestBody UserQuitRoomVo userQuitRoomVo) {
        JSONObject result = userRoomService.userQuitRoom(userid, userQuitRoomVo);
        return result;
    }
}
