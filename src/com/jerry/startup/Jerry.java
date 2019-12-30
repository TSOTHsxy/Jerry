package com.jerry.startup;

import com.jerry.agent.processor.SimpleProcessor;
import com.jerry.extend.CodeAble;
import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.net.connector.Connector;
import com.jerry.net.producer.HttpProducerFactory;
import com.jerry.net.producer.ProducerFactory;
import com.jerry.agent.processor.HttpProcessor;
import com.jerry.agent.processor.Processor;
import com.jerry.agent.adapter.Adapter;
import com.jerry.agent.adapter.DefaultAdapters;
import com.jerry.agent.valve.*;
import com.jerry.net.connector.NIOConnector;
import com.jerry.utils.DefaultContentTypes;
import com.jerry.utils.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Jerry服务器入口
 */
public final class Jerry {

    /**
     * 日志记录器
     */
    private final static Logger logger
            = Logger.getLogger(Jerry.class.getSimpleName());

    /**
     * 默认服务器指令监听端口
     */
    private static final int DEFAULT_COMMAND_PORT = 8085;

    /**
     * 已配置连接器列表
     */
    private List<Connector> connectors = new ArrayList<>();

    /**
     * 全局日志样式
     */
    private LogStyle logStyle;

    /**
     * 全局编码字符集
     */
    private Charset charset;

    /**
     * 服务器指令监听端口
     */
    private int commandPort;

    /**
     * 设置全局日志样式
     *
     * @param logStyle 全局日志样式
     * @return 当前对象实例(用以支持链式调用)
     */
    public Jerry logStyle(LogStyle logStyle) {
        LogUtils.initLogger(logger, logStyle);
        this.logStyle = logStyle;
        return this;
    }

    /**
     * 设置全局编码字符集
     *
     * @param charset 全局编码字符集
     * @return 当前对象实例(用以支持链式调用)
     */
    public Jerry charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置服务器指令监听端口
     *
     * @param commandPort 服务器指令监听端口
     * @return 当前对象实例(用以支持链式调用)
     */
    public Jerry commandPort(int commandPort) {
        this.commandPort = commandPort;
        return this;
    }

    /**
     * 启动服务器
     */
    public void start() {
        if (connectors.isEmpty()) {
            logger.info("Jerry server has not been configured.");
            return;
        }
        boolean hasStyle = logStyle != null && !logStyle.disabled();
        boolean hasCharset = charset != null;
        Connector connector;
        Iterator<Connector> it = connectors.iterator();
        while (it.hasNext()) {
            connector = it.next();
            if (hasStyle && connector instanceof LogAble) {
                ((LogAble) connector).setLogStyle(logStyle);
            }
            if (hasCharset && connector instanceof CodeAble) {
                ((CodeAble) connector).setCharset(charset);
            }
            if (!connector.start()) {
                connector.stop();
                it.remove();
            }
        }
        if (connectors.isEmpty()) {
            logger.info("Jerry server failed to start.");
            return;
        }
        StringBuilder env = new StringBuilder();
        env.append(Platform.environment()).append("\n");
        for (int i = 0; i < connectors.size(); i++) {
            connector = connectors.get(i);
            env.append("Connector-").append(i).append("\n")
                    .append(connector).append("\n");
        }
        env.append("Jerry Server is running now.\n");
        logger.info(env.toString());
        acceptCommand();
    }

    /**
     * 停止服务器
     */
    public void stop() {
        for (Connector connector : connectors) {
            connector.stop();
        }
        logger.info("Jerry Server is stopping now.");
    }

    /**
     * 配置服务
     */
    public MapperBuilder service() {
        return new MapperBuilder();
    }

    /**
     * 自定义配置服务
     *
     * @param factory   请求生成器工厂
     * @param processor 请求处理器
     */
    public ConnectorBuilder design(ProducerFactory factory, Processor processor) {
        return new ConnectorBuilder(processor).factory(factory);
    }

    /**
     * 获取服务器指令监听端口
     */
    private int commandPort() {
        return commandPort == 0 ? DEFAULT_COMMAND_PORT : commandPort;
    }

    /**
     * 监听服务器指令
     */
    private void acceptCommand() {
        try {
            ServerSocket server = new ServerSocket(
                    commandPort(), Platform.isWindows() ? 200 : 128);
            Socket socket;
            BufferedReader in;
            while (true) {
                try {
                    socket = server.accept();
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    try {
                        if ("SHUTDOWN".equals(in.readLine())) {
                            stop();
                            System.exit(0);
                        }
                    } catch (IOException e) {
                        logger.warning(e.getMessage());
                    } finally {
                        socket.close();
                        in.close();
                    }
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }

    /**
     * 映射阀门建造者
     */
    public class MapperBuilder {

        MapperValve valve = new MapperValve();

        MapperBuilder() {
            valve.addAdapters(new DefaultAdapters());
            valve.addContentTypes(new DefaultContentTypes());
        }

        public MapperBuilder mapping(Object object) {
            valve.bindObject(object);
            return this;
        }

        public MapperBuilder addAdapters(Map<Class<?>, Adapter<?>> adapters) {
            valve.addAdapters(adapters);
            return this;
        }

        public MapperBuilder addAdapter(Class<?> clazz, Adapter<?> adapter) {
            valve.addAdapter(clazz, adapter);
            return this;
        }

        public MapperBuilder addContentTypes(Map<String, String> contentTypes) {
            valve.addContentTypes(contentTypes);
            return this;
        }

        public MapperBuilder addContentType(String suffix, String type) {
            valve.addContentType(suffix, type);
            return this;
        }

        public ProcessorBuilder apply() {
            return new ProcessorBuilder(valve);
        }

        public Jerry build() {
            return apply().apply().build();
        }
    }

    /**
     * 请求处理器建造者
     */
    public class ProcessorBuilder {

        SimpleProcessor processor = new HttpProcessor();

        ProcessorBuilder(MapperValve mapperValve) {
            processor.addValve(new LogValve());
            processor.addValve(new DefaultValve());
            processor.addValve(mapperValve);
            processor.addValve(new CheckValve());
            processor.addValve(new InitValve());
        }

        public ProcessorBuilder addValve(Valve valve) {
            processor.addValve(valve);
            return this;
        }

        public ConnectorBuilder apply() {
            return new ConnectorBuilder(processor);
        }

        public Jerry build() {
            return apply().build();
        }
    }

    /**
     * 连接器建造者
     */
    public class ConnectorBuilder {

        Connector connector = new NIOConnector();

        ConnectorBuilder(Processor processor) {
            connector.processor(processor);
        }

        public ConnectorBuilder factory(ProducerFactory factory) {
            connector.factory(factory);
            return this;
        }

        public ConnectorBuilder setUseNagle(boolean useNagle) {
            connector.useNagle(useNagle);
            return this;
        }

        public ConnectorBuilder setKeepAlive(boolean keepAlive) {
            connector.keepAlive(keepAlive);
            return this;
        }

        public ConnectorBuilder backlog(int backlog) {
            connector.backlog(backlog);
            return this;
        }

        public ConnectorBuilder maxThreads(int maxThreads) {
            connector.maxThreads(maxThreads);
            return this;
        }

        public ConnectorBuilder port(int port) {
            connector.port(port);
            return this;
        }

        public Jerry build() {
            if (connector.factory() == null) {
                connector.factory(new HttpProducerFactory());
            }
            connectors.add(connector);
            return Jerry.this;
        }
    }
}
