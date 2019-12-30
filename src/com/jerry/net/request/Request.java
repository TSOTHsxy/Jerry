package com.jerry.net.request;

/**
 * Socket会话请求抽象接口
 */
public interface Request {

    /**
     * @return 请求解析状态
     * 返回{@code true}表示请求解析失败
     * 返回{@code false}表示请求解析成功
     */
    boolean failedParsing();

    /**
     * 设置请求标识
     *
     * @param addr 请求标识
     */
    void addr(String addr);

    /**
     * @return 请求标识
     */
    String addr();

    /**
     * @return 请求地址
     */
    String url();
}
