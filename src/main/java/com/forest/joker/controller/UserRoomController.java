package com.forest.joker.controller;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.annotation.Userid;
import com.forest.joker.exception.JokerAopException;
import com.forest.joker.service.UserRoomService;
import com.forest.joker.utils.JwtUtil;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.*;
import io.jsonwebtoken.Claims;
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
     * 用户通过二维码加入房间
     */
    @PostMapping("/qr/join")
    public Object userJoinRoomByToken(@Userid String userid, @RequestBody UserJoinRoomByQrVo userJoinRoomByQrVo) {
        Claims claims = null;
        try {
            claims = JwtUtil.parseToken(userJoinRoomByQrVo.getQrToken());
        } catch (Exception e) {
            throw new JokerAopException("房间二维码错误~");
        }
        UserJoinRoomVo userJoinRoomVo = new UserJoinRoomVo();
        userJoinRoomVo.setRoomNumber(claims.get("roomNumber", String.class));
        userJoinRoomVo.setRoomPassword(claims.get("roomPassword", String.class));
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

    /**
     * 房主将用户踢出房间
     */
    @PostMapping("/kickOut")
    public Object userKickOut(@Userid String userid, @RequestBody UserKickOutVo userKickOutVo) {
        JSONObject result = userRoomService.userKickOut(userid, userKickOutVo);
        return result;
    }

    /**
     * 转让房主
     */
    @PostMapping("/owner")
    public Object userSetOwner(@Userid String userid, @RequestBody UserSetOwnerVo userSetOwnerVo) {
        JSONObject result = userRoomService.userSetOwner(userid, userSetOwnerVo);
        return result;
    }
}
