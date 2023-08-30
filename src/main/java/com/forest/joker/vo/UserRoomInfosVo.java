package com.forest.joker.vo;

import com.forest.joker.entity.Room;
import com.forest.joker.entity.UserRoom;
import lombok.Data;

import java.util.List;

/**
 * @author: dwh
 **/
@Data
public class UserRoomInfosVo {
    private Room room;
    private List<UserRoomInfoVo> userRooms;
    private String roomOwnerUserId;

    @Data
    public static class UserRoomInfoVo extends UserRoom {
        private String username;
        private String portrait;
    }
}
