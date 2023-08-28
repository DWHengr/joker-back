package com.forest.joker.service;

import com.forest.joker.entity.UserRoom;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forest.joker.vo.UserRoomInfosVo;

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

    boolean quitRoom(String userid);

    boolean joinRoom(String userid, String roomId);
}
