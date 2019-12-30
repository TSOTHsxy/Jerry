package com.jerry.agent.valve;

import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.State;
import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.net.request.HttpRequest;

import java.util.logging.Logger;

/**
 * 请求检查阀门
 * 检查请求对象的合法性
 */
public final class CheckValve extends SimpleValve implements LogAble {

    private final static Logger logger
            = Logger.getLogger(CheckValve.class.getSimpleName());

    private static boolean logSetup = false;

    @Override
    void doProcess(HttpRequest request, HttpResponse response) {
        if (response.state() == State.CHECKED) {
            //检查请求解析状态
            //解析失败视为客户端发起的请求有误
            if (request.failedParsing()) {
                logger.warning(request.addr() + " : Request resolution failed.");
                response.state(State.BAD_REQUEST);
                return;
            }
            //检查请求协议版本
            //当前仅支持HTTP/1.1协议
            String protocol = request.protocol();
            if (!"HTTP/1.1".equals(protocol) && !"HTTP/1.0".equals(protocol)) {
                logger.fine(String.format(request.addr() +
                        " : Unsupported protocol. (current:%s)", protocol));
                response.state(State.HTTP_VERSION_NOT_SUPPORTED);
                return;
            }
            //检查请求url长度
            //请求url的最大长度为2000
            //与chrome浏览器的请求url长度限制相同
            int length = request.url().length();
            if (length > 2000) {
                logger.fine(String.format(request.addr() + " : Request URL " +
                        "length exceeds the maximum limit. (current:%s)", length));
                response.state(State.REQUEST_URI_TOO_LARGE);
                return;
            }
            response.state(State.READY);
        }
    }

    @Override
    public void setLogStyle(LogStyle logStyle) {
        if (logSetup) {
            return;
        }
        LogUtils.initLogger(logger, logStyle);
        logSetup = true;
    }
}
