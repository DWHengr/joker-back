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
 * 用户表
 * </p>
 *
 * @author forest
 * @since 2023-08-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @TableField("account")
    private String account;

    @TableField("name")
    private String name;

    @TableField("password")
    private String password;

    @TableField("sex")
    private String sex;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("portrait")
    private String portrait;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private Long createTime;

    @TableField("update_time")
    private Long updateTime;


}
