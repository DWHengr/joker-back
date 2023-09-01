package com.forest.joker.ws;

import com.forest.joker.constant.WsMsgType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: dwh
 **/
@Data
@AllArgsConstructor
public class WsMsg {
    private WsMsgType type;
    private Object data;
}
