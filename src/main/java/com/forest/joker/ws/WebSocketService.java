package com.forest.joker.ws;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
@ServerEndpoint("/ws/room/{token}")
@Component
@Slf4j
public class WebSocketService {

    @Resource
    JwtUtil jwtUtil;

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> sessionPool = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "token") String token) {
        Claims claims = null;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            return;
        }
        String userId = (String) claims.get("userId");
        String roomId = (String) claims.get("roomId");
        if (null == roomId || null == userId)
            return;
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
    public void onClose(@PathParam(value = "token") String token) {
        Claims claims = null;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            return;
        }
        String userId = (String) claims.get("userId");
        String roomId = (String) claims.get("roomId");
        if (null == roomId || null == userId)
            return;
        ConcurrentHashMap<String, Session> roomSessionPool = sessionPool.get(roomId);
        if (null != roomSessionPool) {
            roomSessionPool.remove(userId);
        }
    }

    /**
     * 退出房间
     */
    public static void quitRoom(String roomId, String userId) {
        ConcurrentHashMap<String, Session> roomSessionPool = sessionPool.get(roomId);
        if (null != roomSessionPool) {
            roomSessionPool.remove(userId);
        }
    }

    /**
     * 解散房间
     */
    public static void dissolveRoom(String roomId) {
        sessionPool.remove(roomId);
    }

    /**
     * 群发消息
     *
     * @param message 发送的消息
     */
    public static void sendAllMessage(String roomId, WsMsg message) {
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
