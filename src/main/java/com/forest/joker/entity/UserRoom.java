package com.forest.joker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户房间表
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_room")
public class UserRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 房间id
     */
    @TableField("room_id")
    private String roomId;

    /**
     * 总分
     */
    @TableField("score")
    private Integer score;

    /**
     * 总分
     */
    @TableField("before_round_score")
    private Integer beforeRoundScore;

    /**
     * 当前状态
     */
    @TableField("status")
    private String status;

    /**
     * 是否庄家
     */
    @TableField("is_dealers")
    private Integer isDealers;

    /**
     * 是否房主
     */
    @TableField("is_owner")
    private Integer isOwner;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Long createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Long updateTime;


}
