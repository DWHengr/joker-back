package com.forest.joker.controller;

import com.forest.joker.annotation.UrlFree;
import com.forest.joker.constant.WsMsgType;
import com.forest.joker.service.UserRoomService;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.UserRoomInfosVo;
import com.forest.joker.ws.WsMsg;
import com.forest.joker.ws.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: dwh
 **/
@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {


    @Resource
    UserRoomService userRoomService;

    @GetMapping("/send")
    @UrlFree
    public Object Test(HttpServletRequest httpServletRequest) {
        Object userinfo = httpServletRequest.getAttribute("userinfo");
        UserRoomInfosVo userRoomInfo = userRoomService.getUserRoomInfo("use-1");
        WebSocketService.sendAllMessage("2e6fcf08-8c45-4eb8-b3bd-719c02bb9c25", new WsMsg(WsMsgType.Info, userRoomInfo));
        return ResultUtil.Succeed(userinfo);
    }
}
