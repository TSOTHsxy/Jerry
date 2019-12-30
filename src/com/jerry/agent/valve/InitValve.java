package com.jerry.agent.valve;

import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.State;
import com.jerry.net.request.HttpRequest;

import java.util.Date;

/**
 * 初始化阀门
 * 初始化响应对象的属性
 */
public final class InitValve extends SimpleValve {

    /**
     * 响应行协议版本
     */
    private static final String PROTOCOL = "HTTP/1.1";

    /**
     * 服务器标识
     */
    private static final String SERVER_IDENTITY =
            String.format("Jerry/1.0 Java/%s", System.getProperty("java.version"));

    @Override
    void doProcess(HttpRequest request, HttpResponse response) {
        if (response.state() == State.UNREADY) {
            response.date(new Date().toString())
                    .protocol(PROTOCOL)
                    .serverIdentity(SERVER_IDENTITY)
                    .state(State.CHECKED);
        }
    }
}
