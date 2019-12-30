package com.jerry.net.producer;

/**
 * 请求生成器工厂抽象接口
 * 统一创建请求生成器实例的方式
 */
public interface ProducerFactory {

    /**
     * @return 请求生成器实例
     * 该方法为{@link com.jerry.net.connector.Connector}对象提供请求生成器
     * 来处理{@link java.nio.channels.SocketChannel}中的字节流
     */
    Producer create();
}
