package com.forest.joker.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: dwh
 **/
@ServerEndpoint("/wb/room/{roomId}/{userid}")
@Component
@Slf4j
public class WebSocketController {


    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> sessionPool = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "roomId") String roomId, @PathParam(value = "userid") String userId) {
        ConcurrentHashMap<String, Session> roomSessionPool = sessionPool.get(roomId);
        if (null == roomSessionPool) {
            roomSessionPool = new ConcurrentHashMap<>();
            sessionPool.put(roomId, roomSessionPool);
        }
        try {
            Session userSession = roomSessionPool.get(userId);
            if (userSession != null) {
                userSession.close();
            }
        } catch (IOException e) {
            log.error("重复登录异常,错误信息: " + e.getMessage(), e);
        }
        // 建立连接
        roomSessionPool.put(userId, session);
        log.info("{}加入房间{}", userId, roomId);
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
    }

    /**
     * 群发消息
     *
     * @param message 发送的消息
     */
    public void sendAllMessage(String roomId, Object message) {
        ConcurrentHashMap<String, Session> roomSession = sessionPool.get(roomId);
        if (null != roomSession) {
            for (Map.Entry<String, Session> userSession : roomSession.entrySet()) {
                try {
                    userSession.getValue().getBasicRemote().sendText(JSONObject.toJSONString(message));
                } catch (Exception e) {
                    roomSession.remove(userSession.getKey());
                    log.error("信息发送错误: " + e.getMessage(), e);
                }
            }
        }
    }

}
