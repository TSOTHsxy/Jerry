package com.jerry.agent.processor;

import com.jerry.agent.response.Response;
import com.jerry.agent.valve.Valve;
import com.jerry.extend.CodeAble;
import com.jerry.extend.Lifecycle;
import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.net.request.Request;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * 请求处理器抽象实现
 */
public abstract class SimpleProcessor implements Processor, Lifecycle, LogAble, CodeAble {

    private final static Logger logger
            = Logger.getLogger(SimpleProcessor.class.getSimpleName());

    private static boolean logSetup = false;

    /**
     * 请求处理管道
     * 存放请求处理阀门
     */
    LinkedList<Valve> pipeline = new LinkedList<>();

    /**
     * 添加请求处理阀门
     *
     * @param valve 请求处理阀门
     */
    public void addValve(Valve valve) {
        pipeline.addFirst(valve);
    }

    @Override
    public Response process(Request request) {
        Response response = makeResponse();
        for (Valve valve : pipeline) {
            valve.process(request, response);
        }
        return response;
    }

    @Override
    public boolean start() {
        if (pipeline.isEmpty()) {
            logger.warning("No valves are =added to the current request processor.");
            return false;
        }
        for (Valve valve : pipeline) {
            if (valve instanceof Lifecycle && !((Lifecycle) valve).start()) {
                return false;
            }
        }
        logger.config("Processor startup now.");
        return true;
    }

    @Override
    public void stop() {
        logger.config("Processor stop now.");
    }

    @Override
    public void setLogStyle(LogStyle logStyle) {
        if (logSetup) {
            return;
        }
        LogUtils.initLogger(logger, logStyle);
        for (Valve valve : pipeline) {
            if (valve instanceof LogAble) {
                ((LogAble) valve).setLogStyle(logStyle);
            }
        }
        logSetup = true;
    }

    @Override
    public void setCharset(Charset charset) {
        for (Valve valve : pipeline) {
            if (valve instanceof CodeAble) {
                ((CodeAble) valve).setCharset(charset);
            }
        }
    }

    /**
     * @return 响应对象
     * 该方法为{@link this#process(Request)}方法提供初始的响应对象
     * 通过实现该方法可适应不同的响应类型
     */
    protected abstract Response makeResponse();
}
