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
 * 房间表
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("room")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @TableId("id")
    private String id;

    /**
     * 房间号
     */
    @TableField("number")
    private String number;

    /**
     * 房间名称
     */
    @TableField("name")
    private String name;

    /**
     * 房间类型
     */
    @TableField("type")
    private String type;

    /**
     * 房间密码
     */
    @TableField("password")
    private String password;

    /**
     * 房间状态
     */
    @TableField("status")
    private String status;

    /**
     * 当前轮次
     */
    @TableField("round")
    private Integer round;

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
