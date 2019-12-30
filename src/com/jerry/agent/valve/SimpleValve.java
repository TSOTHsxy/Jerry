package com.jerry.agent.valve;

import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.Response;
import com.jerry.net.request.HttpRequest;
import com.jerry.net.request.Request;

/**
 * HTTP协议请求处理阀门抽象实现
 */
abstract class SimpleValve implements Valve {

    @Override
    public void process(Request request, Response response) {
        if (request instanceof HttpRequest && response instanceof HttpResponse) {
            doProcess((HttpRequest) request, (HttpResponse) response);
        }
    }

    /**
     * 抽象委派方法
     * 通过在{@link this#process(Request, Response)}方法中将参数委派给该方法处理
     * 实现了参数具象化的效果从而简化了在阀门中的类型检查
     *
     * @param request  请求对象
     * @param response 响应对象
     */
    abstract void doProcess(HttpRequest request, HttpResponse response);
}
