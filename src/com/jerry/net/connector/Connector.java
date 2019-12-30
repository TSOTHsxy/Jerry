package com.jerry.net.connector;

import com.jerry.agent.processor.Processor;
import com.jerry.net.producer.ProducerFactory;
import com.jerry.extend.Lifecycle;
import com.jerry.utils.Platform;

/**
 * 连接器抽象实现
 * 本项目以NIO技术实现此抽象类
 */
public abstract class Connector implements Lifecycle {

    /**
     * 默认连接队列长度
     * Windows: 200
     * Linux or MacOS: 128
     */
    protected static final int SOMAXCONN = Platform.isWindows() ? 200 : 128;

    /**
     * 默认读写线程数量
     * 值为系统可用的处理器数量的两倍
     */
    protected static final int DEFAULT_MAX_THREADS
            = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 默认监听端口
     */
    protected static final int DEFAULT_PORT = 8090;

    /**
     * 请求生成器工厂
     */
    protected ProducerFactory factory;

    /**
     * 请求处理器
     */
    protected Processor processor;

    /**
     * 是否启用Nagle‘s算法
     */
    protected boolean useNagle = false;

    /**
     * 是否保持长连接
     */
    protected boolean keepAlive = true;

    /**
     * 连接队列长度
     */
    protected int backlog;

    /**
     * 读写线程数量
     */
    protected int maxThreads;

    /**
     * 监听端口
     */
    protected int port;

    /**
     * 设置请求生成器工厂
     *
     * @param factory 请求生成器工厂
     */
    public void factory(ProducerFactory factory) {
        this.factory = factory;
    }

    /**
     * @return 请求生成器工厂
     */
    public ProducerFactory factory() {
        return factory;
    }

    /**
     * 设置请求处理器
     *
     * @param processor 请求处理器
     */
    public void processor(Processor processor) {
        this.processor = processor;
    }

    /**
     * @return 请求处理器
     */
    public Processor processor() {
        return processor;
    }

    /**
     * 设置是否启用Nagle‘s算法
     *
     * @param useNagle 是否启用Nagle‘s算法
     */
    public void useNagle(boolean useNagle) {
        this.useNagle = useNagle;
    }

    /**
     * @return 是否启用Nagle‘s算法
     */
    public boolean isUseNagle() {
        return useNagle;
    }

    /**
     * 设置是否保持长连接
     *
     * @param keepAlive 是否保持长连接
     */
    public void keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * @return 是否保持长连接
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }

    /**
     * 设置连接队列长度
     *
     * @param backlog 连接队列长度
     */
    public void backlog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * @return 连接队列长度
     */
    public int backlog() {
        return backlog == 0 ? SOMAXCONN : backlog;
    }

    /**
     * 设置读写线程数量
     *
     * @param maxThreads 读写线程数量
     */
    public void maxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * @return 读写线程数量
     */
    public int maxThreads() {
        return maxThreads == 0 ? DEFAULT_MAX_THREADS : maxThreads;
    }

    /**
     * 设置监听端口
     *
     * @param port 监听端口
     */
    public void port(int port) {
        this.port = port;
    }

    /**
     * @return 监听端口
     */
    public int port() {
        return port == 0 ? DEFAULT_PORT : port;
    }

    @Override
    public String toString() {
        return "Accept on http://" +
                Platform.address() + ":" + port() + "/\n" +
                "Backlog    : " + backlog() + "\n" +
                "MaxThreads : " + maxThreads() + "\n" +
                "UseNagle   : " + isUseNagle() + "\n" +
                "KeepAlive  : " + isKeepAlive() + "\n";
    }
}
