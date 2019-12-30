package com.jerry.agent.valve;

import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.net.request.HttpRequest;
import com.jerry.agent.response.HttpResponse;
import com.jerry.agent.response.State;

import java.util.logging.Logger;

/**
 * 日志记录阀门
 * 记录响应日志
 */
public final class LogValve extends SimpleValve implements LogAble {

    private final static Logger logger
            = Logger.getLogger(LogValve.class.getSimpleName());

    private static boolean logSetup = false;

    @Override
    void doProcess(HttpRequest request, HttpResponse response) {
        if (response.state() == State.COMPLETE) {
            logger.info(String.format("%s : %s %s %s %s %s", request.addr(),
                    request.method(), request.url(), request.protocol(),
                    response.code(), response.reason()));
        } else {
            logger.warning("Request processing " +
                    "not completed, please check valve settings.");
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
