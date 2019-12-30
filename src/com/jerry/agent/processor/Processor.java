package com.jerry.agent.processor;

import com.jerry.agent.response.Response;
import com.jerry.net.request.Request;

/**
 * 请求处理器抽象接口
 * 建议使用职责链模式处理请求
 */
public interface Processor {

    /**
     * 处理Socket会话请求并获取对应的响应对象
     *
     * @param request 请求对象
     * @return 响应对象
     */
    Response process(Request request);
}
