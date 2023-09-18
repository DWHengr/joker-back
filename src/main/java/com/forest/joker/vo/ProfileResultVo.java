package com.forest.joker.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ProfileResultVo {

    private String account;

    private String name;

    private String sex;

    private String phone;

    private String email;

    private String portrait;

    private String status;
}
