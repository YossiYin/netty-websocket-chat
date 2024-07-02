package com.yen.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Yhx
 * @date 2024/7/1 10:48
 */
@Data
public class BaseRequest implements Serializable {
    /**
     * 请求的具体消息
     */
    private String reqMsg;
    /**
     * 唯一请求id(用于记录客户端的连接信息key值)
     */
    private long reqId;
    /**
     * 消息类型(1建立连接2业务消息3心跳ping)
     */
    private int type;
}
