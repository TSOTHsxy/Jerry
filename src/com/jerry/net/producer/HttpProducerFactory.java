package com.jerry.net.producer;

import com.jerry.net.request.HttpRequest;
import com.jerry.net.request.Request;
import com.jerry.extend.CodeAble;
import com.jerry.utils.buffer.ByteBuff;
import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * HTTP协议请求生成器工厂
 */
public final class HttpProducerFactory implements ProducerFactory, LogAble, CodeAble {

    private final static Logger logger
            = Logger.getLogger(HttpProducerFactory.class.getSimpleName());

    private static boolean logSetup = false;

    private Charset charset;

    @Override
    public Producer create() {
        return new Producer() {

            /**
             * Socket会话通道
             */
            private SocketChannel channel;

            /**
             * 请求标识
             */
            private String addr;

            /**
             * 临时缓冲区
             */
            private ByteBuffer buffer = ByteBuffer.allocate(1024);

            /**
             * 动态扩容缓冲区
             */
            private ByteBuff buff = new ByteBuff();

            @Override
            public SocketChannel channel() {
                return channel;
            }

            @Override
            public int readOnChannel() throws IOException {
                int count, readBytes;
                //分段读取字节流
                while ((count = channel().read(buffer)) > 0) {
                    buffer.flip();
                    readBytes = buff.write(buffer);
                    if (readBytes == 0) {
                        break;
                    }
                    //实际缓存字节数和有效读取字节数不匹配
                    if (count != readBytes) {
                        logger.warning(addr +
                                " : The length of characters written in the" +
                                " primary and secondary cache is inconsistent.");
                    }
                    buffer.clear();
                }
                return count;
            }

            @Override
            public Request makeRequest() {
                if (buff.isEmpty()) {
                    logger.warning(addr + " : No data was obtained from the channel.");
                    return null;
                }
                HttpRequest request = new HttpRequest(buff.array());
                request.addr(addr);
                if (charset != null) {
                    request.setCharset(charset);
                } else {
                    request.setCharset(StandardCharsets.UTF_8);
                }
                flush();
                request.parse();
                return request;
            }

            @Override
            public void reset(SocketChannel obj) {
                channel = obj;
                addr = channel.socket().getInetAddress().toString();
            }

            @Override
            public void clear() {
                addr = null;
                channel = null;
                flush();
            }

            /**
             * 清空字节流缓冲区
             */
            private void flush() {
                buff.clear();
                buffer.clear();
            }

            @Override
            public int hashCode() {
                return buffer.hashCode() ^ buff.hashCode();
            }
        };
    }

    @Override
    public void setLogStyle(LogStyle logStyle) {
        if (logSetup) {
            return;
        }
        LogUtils.initLogger(logger, logStyle);
        logSetup = true;
    }

    @Override
    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
