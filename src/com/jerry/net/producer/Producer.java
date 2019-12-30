package com.jerry.net.producer;

import com.jerry.net.request.Request;
import com.jerry.extend.Reusable;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 请求生成器抽象接口
 */
public interface Producer extends Reusable<SocketChannel> {

    /**
     * @return Socket会话通道
     */
    SocketChannel channel();

    /**
     * 从通道中读取数据
     * 当此方法返回-1表示本次Socket会话完毕
     * 此时应妥善关闭通道并清理缓冲区
     *
     * @return 有效的读取字节数
     * @throws IOException 从通道中读取数据失败
     */
    int readOnChannel() throws IOException;

    /**
     * @return 请求对象
     * 可以在此方法内解析请求
     * 也可以将解析工作移交给{@link Request}对象处理
     */
    Request makeRequest();
}
