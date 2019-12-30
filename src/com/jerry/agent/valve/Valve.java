package com.jerry.agent.valve;

import com.jerry.agent.processor.Processor;
import com.jerry.agent.response.Response;
import com.jerry.net.request.Request;

/**
 * 请求处理阀门抽象接口
 */
public interface Valve {

    /**
     * 处理Socket会话请求并获取对应的响应对象
     * 该方法一般被{@link Processor#process(Request)}方法调用
     * 是实际处理请求生成响应的场所
     *
     * @param request  请求对象
     * @param response 响应对象
     */
    void process(Request request, Response response);
}
