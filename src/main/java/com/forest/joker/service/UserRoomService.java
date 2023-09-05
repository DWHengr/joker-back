package com.forest.joker.service;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.entity.UserRoom;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forest.joker.vo.*;

/**
 * <p>
 * 用户房间表 服务类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
public interface UserRoomService extends IService<UserRoom> {

    UserRoomInfosVo getUserRoomInfo(String userid);

    UserRoom getPersonalUserRoomInfo(String userid);

    boolean quitRoom(String userid);

    boolean joinRoom(String userid, String roomId, int dealers, int owner);

    JSONObject userJoinRoom(String userid, UserJoinRoomVo userJoinRoomVo);

    JSONObject userQuitRoom(String userid, UserQuitRoomVo userQuitRoomVo);

    JSONObject createQrToken(String userid);

    JSONObject userKickOut(String userid, UserKickOutVo userKickOutVo);

    JSONObject userSetOwner(String userid, UserSetOwnerVo userSetOwnerVo);
}
