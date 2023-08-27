package com.forest.joker.exception;

import com.alibaba.fastjson.JSONObject;
import com.forest.joker.utils.ResultUtil;

import java.util.HashMap;

/**
 * @author: dwh
 **/
public class JokerAopException extends RuntimeException {

    private int code;
    private String message;
    private HashMap<String, Object> param;

    public JokerAopException(String message) {
        this.code = ResultUtil.ResponseEnum.FAIL.getType();
        this.message = message;
    }

    /***
     * 添加异常信息 键值对
     */
    public JokerAopException param(String key, Object value) {
        if (null == this.param) {
            this.param = new HashMap<>();
        }
        param.put(key, value);
        return this;
    }

    /***
     * 置空param
     */
    public JokerAopException empty() {
        this.param = new HashMap<>();
        return this;
    }

    public JokerAopException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String paramToString() {
        if (null == this.param || this.param.size() <= 0)
            return null;
        return JSONObject.toJSONString(this.param);
    }
}
