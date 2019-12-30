package com.jerry.agent.processor;

import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.Response;

/**
 * HTTP协议请求处理器默认实现
 */
public final class HttpProcessor extends SimpleProcessor {

    @Override
    protected Response makeResponse() {
        return new HttpResponse();
    }
}
