package com.forest.joker.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.forest.joker.constant.UserRoomStatus;
import com.forest.joker.constant.WsMsgType;
import com.forest.joker.entity.Room;
import com.forest.joker.entity.User;
import com.forest.joker.entity.UserRoom;
import com.forest.joker.exception.JokerAopException;
import com.forest.joker.mapper.UserRoomMapper;
import com.forest.joker.service.RoomService;
import com.forest.joker.service.UserRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forest.joker.service.UserService;
import com.forest.joker.utils.JwtUtil;
import com.forest.joker.utils.RandomUtil;
import com.forest.joker.utils.ResultUtil;
import com.forest.joker.vo.*;
import com.forest.joker.ws.WebSocketService;
import com.forest.joker.ws.WsMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 用户房间表 服务实现类
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Service
@Slf4j
public class UserRoomServiceImpl extends ServiceImpl<UserRoomMapper, UserRoom> implements UserRoomService {

    @Resource
    RoomService roomService;

    @Resource
    UserService userService;

    @Override
    public UserRoom getPersonalUserRoomInfo(String userid) {
        //获取当前用户房间id
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        return userRoom;

    }

    @Override
    public UserRoomInfosVo getUserRoomInfo(String userid) {
        //获取当前用户房间id
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);

        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        if (null != userRoom)
            return getUserRoomInfoByRoomId(userRoom.getRoomId());

        return null;

    }

    @Override
    public boolean quitRoom(String userid) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);
        return remove(userRoomLambdaQueryWrapper);
    }

    @Override
    public boolean joinRoom(String userid, String roomId, int dealers, int owner) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid).eq(UserRoom::getRoomId, roomId);
        int count = count(userRoomLambdaQueryWrapper);
        if (count > 0)
            return true;
        UserRoom userRoom = new UserRoom();
        userRoom.setId(RandomUtil.generateUuid());
        userRoom.setRoomId(roomId);
        userRoom.setUserId(userid);
        userRoom.setScore(0);
        userRoom.setRoundScore(0);
        userRoom.setIsDealers(dealers);
        userRoom.setIsOwner(owner);
        userRoom.setCreateTime(System.currentTimeMillis());
        userRoom.setUpdateTime(System.currentTimeMillis());
        return save(userRoom);
    }

    @Override
    public JSONObject userJoinRoom(String userid, UserJoinRoomVo userJoinRoomVo) {
        //验证房间密码
        Room room = roomService.getRoomInfoByNumber(userJoinRoomVo.getRoomNumber());
        if (null == room) {
            throw new JokerAopException("房间不存在~").param("roomId", userJoinRoomVo.getRoomNumber());
        }
        if (!room.getPassword().equals(userJoinRoomVo.getRoomPassword())) {
            throw new JokerAopException("房间密码错误~").param("roomId", userJoinRoomVo.getRoomNumber());
        }
        boolean flag = joinRoom(userid, room.getId(), 0, 0);
        WebSocketService.sendAllMessage(room.getId(), new WsMsg(WsMsgType.Info, getUserRoomInfo(userid)));
        if (flag)
            return ResultUtil.Succeed(roomService.createWsTokenInfo(userid, room));
        else
            return ResultUtil.Fail();
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public JSONObject userQuitRoom(String userid, UserQuitRoomVo userQuitRoomVo) {
        //根据用户id和房间id，获取用户房间信息
        UserRoom userRoom = getUserRoomByUserIdAndRoomId(userid, userQuitRoomVo.getRoomId());
        if (null == userRoom)
            return ResultUtil.Succeed();
        boolean flag = false;
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (userRoom.getIsOwner() == 1) {
            //用户为当前房间房主时，解散房间
            userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, userRoom.getRoomId());
            roomService.removeById(userRoom.getRoomId());
            flag = remove(userRoomLambdaQueryWrapper);
            //发送解散房间消息
            WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Quit, null));
            WebSocketService.dissolveRoom(userRoom.getRoomId());
        } else {
            userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, userRoom.getRoomId())
                    .eq(UserRoom::getUserId, userid);
            //退出房间
            flag = remove(userRoomLambdaQueryWrapper);
            WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userRoom.getRoomId())));
            WebSocketService.quitRoom(userRoom.getRoomId(), userid);
        }
        if (flag)
            return ResultUtil.Succeed();
        else
            return ResultUtil.Fail();
    }

    @Override
    public JSONObject createQrToken(String userid) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userid);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        if (null == userRoom) {
            return ResultUtil.Fail("房间不存在~");
        }
        Room room = roomService.getById(userRoom.getRoomId());
        if (null == room) {
            return ResultUtil.Fail("房间不存在~");
        }
        HashMap<String, Object> info = new HashMap<>();
        info.put("roomNumber", room.getNumber());
        info.put("roomPassword", room.getPassword());
        String token = JwtUtil.createToken(info);
        JSONObject resultJson = new JSONObject();
        resultJson.put("qrToken", token);
        return ResultUtil.Succeed(resultJson);
    }

    @Override
    public JSONObject userKickOut(String userid, UserKickOutVo userKickOutVo) {
        UserRoom ownerByRoomId = getOwnerByRoomId(userKickOutVo.getRoomId());
        if (null == ownerByRoomId) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (!ownerByRoomId.getUserId().equals(userid)) {
            return ResultUtil.Fail("您不是房主~");
        }
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, userKickOutVo.getRoomId())
                .eq(UserRoom::getUserId, userKickOutVo.getUserId());
        //踢出房间
        boolean flag = remove(userRoomLambdaQueryWrapper);
        WebSocketService.sendUserMessage(userKickOutVo.getUserId(), new WsMsg(WsMsgType.Kick, null));
        WebSocketService.quitRoom(userKickOutVo.getRoomId(), userKickOutVo.getUserId());
        WebSocketService.sendAllMessage(userKickOutVo.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userKickOutVo.getRoomId())));
        if (flag)
            return ResultUtil.Succeed();
        else
            return ResultUtil.Fail();
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public JSONObject userSetOwner(String userid, UserSetOwnerVo userSetOwnerVo) {
        UserRoom ownerByRoomId = getOwnerByRoomId(userSetOwnerVo.getRoomId());
        if (null == ownerByRoomId) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (!ownerByRoomId.getUserId().equals(userid)) {
            return ResultUtil.Fail("您不是房主~");
        }
        LambdaUpdateWrapper<UserRoom> userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper.set(UserRoom::getIsOwner, 0)
                .eq(UserRoom::getRoomId, userSetOwnerVo.getRoomId())
                .eq(UserRoom::getUserId, userid);
        boolean flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("房主转让失败~");
        }
        //转让房主
        userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper.set(UserRoom::getIsOwner, 1)
                .eq(UserRoom::getRoomId, userSetOwnerVo.getRoomId())
                .eq(UserRoom::getUserId, userSetOwnerVo.getUserId());
        flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("房主转让失败~");
        }
        WebSocketService.sendAllMessage(userSetOwnerVo.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userSetOwnerVo.getRoomId())));
        return ResultUtil.Succeed();

    }

    @Override
    public JSONObject userSetDealers(String userid, UserSetDealersVo userSetDealersVo) {
        UserRoom ownerByRoomId = getOwnerByRoomId(userSetDealersVo.getRoomId());
        if (null == ownerByRoomId) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (!ownerByRoomId.getUserId().equals(userid)) {
            return ResultUtil.Fail("您不是房主~");
        }
        //查询当前庄家
        UserRoom dealersByRoomId = getDealersByRoomId(userSetDealersVo.getRoomId());
        LambdaUpdateWrapper<UserRoom> userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper.set(UserRoom::getIsDealers, 0)
                .eq(UserRoom::getRoomId, userSetDealersVo.getRoomId())
                .eq(UserRoom::getUserId, dealersByRoomId.getUserId());
        boolean flag = update(userRoomLambdaUpdateWrapper);

        if (!flag) {
            throw new JokerAopException("庄家设置失败~");
        }
        //设置庄家
        userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper.set(UserRoom::getIsDealers, 1)
                .eq(UserRoom::getRoomId, userSetDealersVo.getRoomId())
                .eq(UserRoom::getUserId, userSetDealersVo.getUserId());
        flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("庄家设置失败~");
        }
        WebSocketService.sendAllMessage(userSetDealersVo.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userSetDealersVo.getRoomId())));
        return ResultUtil.Succeed();
    }

    @Override
    public JSONObject userScoreAdd1(String userid, UserScoreAdd1Vo userScoreAdd1Vo) {
        UserRoom ownerByRoomId = getOwnerByRoomId(userScoreAdd1Vo.getRoomId());
        if (null == ownerByRoomId) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (!ownerByRoomId.getUserId().equals(userid)) {
            return ResultUtil.Fail("您不是房主~");
        }
        UserRoom userRoom = getUserRoom(userScoreAdd1Vo.getRoomId(), userScoreAdd1Vo.getUserId());
        if (null == userRoom) {
            return ResultUtil.Fail("成员不存在~");
        }
        //分数加1
        LambdaUpdateWrapper<UserRoom> userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper.set(UserRoom::getScore, userRoom.getScore() + 1)
                .eq(UserRoom::getRoomId, userScoreAdd1Vo.getRoomId())
                .eq(UserRoom::getUserId, userScoreAdd1Vo.getUserId());
        boolean flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("分数增加失败~");
        }
        WebSocketService.sendAllMessage(userScoreAdd1Vo.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userScoreAdd1Vo.getRoomId())));
        return ResultUtil.Succeed();
    }

    @Override
    public JSONObject userScoreSubtract1(String userid, UserScoreAdd1Vo userScoreAdd1Vo) {
        UserRoom ownerByRoomId = getOwnerByRoomId(userScoreAdd1Vo.getRoomId());
        if (null == ownerByRoomId) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (!ownerByRoomId.getUserId().equals(userid)) {
            return ResultUtil.Fail("您不是房主~");
        }
        UserRoom userRoom = getUserRoom(userScoreAdd1Vo.getRoomId(), userScoreAdd1Vo.getUserId());
        if (null == userRoom) {
            return ResultUtil.Fail("成员不存在~");
        }
        //分数加1
        LambdaUpdateWrapper<UserRoom> userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper.set(UserRoom::getScore, userRoom.getScore() - 1)
                .eq(UserRoom::getRoomId, userScoreAdd1Vo.getRoomId())
                .eq(UserRoom::getUserId, userScoreAdd1Vo.getUserId());
        boolean flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("分数减少失败~");
        }
        WebSocketService.sendAllMessage(userScoreAdd1Vo.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userScoreAdd1Vo.getRoomId())));
        return ResultUtil.Succeed();
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public JSONObject userScoreSubmit(String userid, UserScoreSubmitVo userScoreSubmitVo) {
        //获取该房间所有成员
        UserRoom userRoom = getPersonalUserRoomInfo(userid);
        if (null == userRoom) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (userRoom.getIsDealers() == 1) {
            return ResultUtil.Fail("您是庄家,请等待其他成员结算~");
        }
        if (UserRoomStatus.Settled.toString().equals(userRoom.getStatus())) {
            return ResultUtil.Fail("您已结算,请求先撤销~");
        }
        //分数改变
        LambdaUpdateWrapper<UserRoom> userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper
                .set(UserRoom::getRoundScore, userScoreSubmitVo.getScore())
                .set(UserRoom::getScore, userRoom.getScore() + userScoreSubmitVo.getScore())
                .set(UserRoom::getStatus, UserRoomStatus.Settled)
                .eq(UserRoom::getRoomId, userRoom.getRoomId())
                .eq(UserRoom::getUserId, userid);
        boolean flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("分数提交失败~");
        }
        //获取庄家
        UserRoom dealersUserRoom = getDealersByRoomId(userRoom.getRoomId());
        if (null != dealersUserRoom) {
            //庄家分数改变
            userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userRoomLambdaUpdateWrapper
                    .set(UserRoom::getRoundScore, dealersUserRoom.getRoundScore() - userScoreSubmitVo.getScore())
                    .set(UserRoom::getScore, dealersUserRoom.getScore() - userScoreSubmitVo.getScore())
                    .set(UserRoom::getStatus, UserRoomStatus.Settled)
                    .eq(UserRoom::getRoomId, dealersUserRoom.getRoomId())
                    .eq(UserRoom::getUserId, dealersUserRoom.getUserId());
            flag = update(userRoomLambdaUpdateWrapper);
            if (!flag) {
                throw new JokerAopException("分数提交失败~");
            }
        }
        WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userRoom.getRoomId())));
        return ResultUtil.Succeed();
    }

    @Override
    public JSONObject userScoreAnnul(String userid) {
        UserRoom userRoom = getPersonalUserRoomInfo(userid);
        if (null == userRoom) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (userRoom.getIsDealers() == 1) {
            return ResultUtil.Fail("您是庄家,请等待其他成员结算~");
        }
        if (!UserRoomStatus.Settled.toString().equals(userRoom.getStatus())) {
            return ResultUtil.Fail("您为庄家,无需撤销~");
        }
        //分数改变
        LambdaUpdateWrapper<UserRoom> userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userRoomLambdaUpdateWrapper
                .set(UserRoom::getRoundScore, 0)
                .set(UserRoom::getScore, userRoom.getScore() - userRoom.getRoundScore())
                .set(UserRoom::getStatus, UserRoomStatus.Unsettled)
                .eq(UserRoom::getRoomId, userRoom.getRoomId())
                .eq(UserRoom::getUserId, userid);
        boolean flag = update(userRoomLambdaUpdateWrapper);
        if (!flag) {
            throw new JokerAopException("分数撤销失败~");
        }
        //获取庄家
        UserRoom dealersUserRoom = getDealersByRoomId(userRoom.getRoomId());
        if (null != dealersUserRoom) {
            //庄家分数改变
            userRoomLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userRoomLambdaUpdateWrapper
                    .set(UserRoom::getRoundScore, dealersUserRoom.getRoundScore() + userRoom.getRoundScore())
                    .set(UserRoom::getScore, dealersUserRoom.getScore() + userRoom.getRoundScore())
                    .eq(UserRoom::getRoomId, dealersUserRoom.getRoomId())
                    .eq(UserRoom::getUserId, dealersUserRoom.getUserId());
            flag = update(userRoomLambdaUpdateWrapper);
            if (!flag) {
                throw new JokerAopException("分数撤销失败~");
            }
        }
        WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userRoom.getRoomId())));
        return ResultUtil.Succeed();
    }

    @Override
    @Transactional(rollbackFor = {RuntimeException.class})
    public JSONObject userRoomStart(String userid) {
        UserRoom userRoom = getPersonalUserRoomInfo(userid);
        if (null == userRoom) {
            return ResultUtil.Fail("房间不存在~");
        }
        if (userRoom.getIsOwner() != 1) {
            return ResultUtil.Fail("您不是房主~");
        }
        List<UserRoom> userRooms = listByRoomId(userRoom.getRoomId());
        long settledNum = userRooms.stream().filter(v -> UserRoomStatus.Settled.toString().equals(v.getStatus())).count();
        //除去庄家，所有人都结算
        if (settledNum >= userRooms.size()) {
            //开启下一轮
            roomService.updateRoomRound(userRoom.getRoomId());
            //更分数
            LambdaUpdateWrapper<UserRoom> userRoomUpdateWrapper = new LambdaUpdateWrapper<>();
            userRoomUpdateWrapper.set(UserRoom::getRoundScore, 0)
                    .set(UserRoom::getStatus, UserRoomStatus.Unsettled.toString())
                    .eq(UserRoom::getRoomId, userRoom.getRoomId());
            update(userRoomUpdateWrapper);
            WebSocketService.sendAllMessage(userRoom.getRoomId(), new WsMsg(WsMsgType.Info, getUserRoomInfoByRoomId(userRoom.getRoomId())));
        } else {
            return ResultUtil.Fail("等待其他成员结算~");
        }
        return ResultUtil.Succeed();
    }

    public List<UserRoom> listByRoomId(String roomId) {
        LambdaQueryWrapper<UserRoom> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserRoom::getRoomId, roomId);
        return list(lambdaQueryWrapper);
    }

    public UserRoom getUserRoomByUserIdAndRoomId(String userId, String roomId) {
        LambdaQueryWrapper<UserRoom> roomLambdaQueryWrapper = new LambdaQueryWrapper();
        roomLambdaQueryWrapper.eq(UserRoom::getRoomId, roomId).eq(UserRoom::getUserId, userId);
        UserRoom userRoom = getOne(roomLambdaQueryWrapper);
        return userRoom;
    }

    /**
     * 获取房间房主
     *
     * @return
     */
    public UserRoom getOwnerByRoomId(String roomId) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getIsOwner, 1).eq(UserRoom::getRoomId, roomId);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        return userRoom;
    }

    /**
     * 获取房间庄家
     *
     * @return
     */
    public UserRoom getDealersByRoomId(String roomId) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getIsDealers, 1).eq(UserRoom::getRoomId, roomId);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        return userRoom;
    }

    /**
     * 根据用户id和房间id获取用户房间信息
     *
     * @return
     */
    public UserRoom getUserRoom(String roomId, String userId) {
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getUserId, userId).eq(UserRoom::getRoomId, roomId);
        UserRoom userRoom = getOne(userRoomLambdaQueryWrapper);
        return userRoom;
    }


    /**
     * 根据房间号获取房间的用户信息
     */
    public UserRoomInfosVo getUserRoomInfoByRoomId(String roomId) {
        //房间信息
        Room room = roomService.getById(roomId);
        if (null == room)
            return null;
        UserRoomInfosVo userRoomInfosVo = new UserRoomInfosVo();
        userRoomInfosVo.setRoom(room);

        //房间所有用户信息
        LambdaQueryWrapper<UserRoom> userRoomLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userRoomLambdaQueryWrapper.eq(UserRoom::getRoomId, roomId).orderByDesc(UserRoom::getScore);
        List<UserRoom> userRooms = list(userRoomLambdaQueryWrapper);
        List<UserRoomInfosVo.UserRoomInfoVo> userRoomInfoList = new ArrayList<>();

        String roomOwnerUserId = "";
        String roomDealersUserId = "";
        for (UserRoom userRoom : userRooms) {
            UserRoomInfosVo.UserRoomInfoVo userRoomInfoVo = new UserRoomInfosVo.UserRoomInfoVo();
            BeanUtils.copyProperties(userRoom, userRoomInfoVo);
            //设置用户信息
            User user = userService.getById(userRoom.getUserId());
            userRoomInfoVo.setUsername(user.getName());
//            userRoomInfoVo.setPortrait(user.getPortrait());
            userRoomInfoList.add(userRoomInfoVo);
            if (userRoom.getIsOwner() == 1) {
                roomOwnerUserId = userRoom.getUserId();
            }
            if (userRoom.getIsDealers() == 1) {
                roomDealersUserId = userRoom.getUserId();
            }
        }
        userRoomInfosVo.setRoomDealersUserId(roomDealersUserId);
        userRoomInfosVo.setRoomOwnerUserId(roomOwnerUserId);
        userRoomInfosVo.setUserRooms(userRoomInfoList);
        return userRoomInfosVo;
    }
}
