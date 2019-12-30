package com.jerry.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 通道读写工具类
 */
public final class ChannelUtils {

    //通道读写阀值
    private static int NIO_BUFFER_LIMIT = 64 * 1024;

    /**
     * 从通道中读取数据
     *
     * @param channel IO通道
     * @param buffer  字节缓冲区
     * @return 有效的读取字节数
     */
    public static int read(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
        return buffer.remaining() <= NIO_BUFFER_LIMIT
                ? channel.read(buffer)
                : IO(channel, null, buffer);
    }

    /**
     * 向通道中写入数据
     *
     * @param channel IO通道
     * @param buffer  字节缓冲区
     * @return 有效的写入字节数
     */
    public static int write(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
        return buffer.remaining() <= NIO_BUFFER_LIMIT
                ? channel.write(buffer)
                : IO(null, channel, buffer);
    }

    /**
     * 分段读取/写入数据
     * 避免一次性读写大量数据导致OOM异常
     */
    private static int IO(ReadableByteChannel rc, WritableByteChannel wc,
                          ByteBuffer buffer) throws IOException {
        //记录缓冲区原始信息
        int remaining = buffer.remaining();
        int limit = buffer.limit();
        int size, eff = 0;
        try {
            while (buffer.remaining() > 0) {
                //计算本次读/写字节数
                size = Math.min(buffer.remaining(), NIO_BUFFER_LIMIT);
                buffer.limit(buffer.position() + size);
                eff = rc == null ? wc.write(buffer) : rc.read(buffer);
                if (eff < size) {
                    break;
                }
            }
        } finally {
            buffer.limit(limit);
        }
        //计算有效的读取/写入字节数
        int effBytes = remaining - buffer.remaining();
        return effBytes > 0 ? effBytes : eff;
    }
}
