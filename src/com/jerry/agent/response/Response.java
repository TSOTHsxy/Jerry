package com.jerry.agent.response;

/**
 * Socket会话响应抽象接口
 */
public interface Response {

    /**
     * @return 响应内容字节流
     * 该字节流随后一般被写入到网络文件描述符中
     */
    byte[] toBytes();
}
