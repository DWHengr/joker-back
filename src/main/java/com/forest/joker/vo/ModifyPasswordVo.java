package com.forest.joker.vo;

import lombok.Data;

@Data
public class ModifyPasswordVo {
    private String oldPassword;
    private String newPassword;
}
