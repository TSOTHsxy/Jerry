package com.jerry.utils;

import com.jerry.logger.LogAble;
import com.jerry.logger.LogStyle;
import com.jerry.logger.LogUtils;
import com.jerry.utils.buffer.ByteBuff;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * 资源管理工具类
 */
public final class Resources implements LogAble {

    private final static Logger logger
            = Logger.getLogger(Resources.class.getSimpleName());

    private static boolean logSetup = false;

    @Override
    public void setLogStyle(LogStyle logStyle) {
        if (logSetup) {
            return;
        }
        LogUtils.initLogger(logger, logStyle);
        logSetup = true;
    }

    /**
     * 读取本地文件
     * 读取文件的大小受{@link ByteBuff}缓冲区最大容量的限制
     *
     * @param file 文件路径
     * @return 本地文件字节流
     */
    public byte[] readFile(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            FileChannel channel = stream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            ByteBuff buff = new ByteBuff();
            try {
                while (channel.read(buffer) != -1) {
                    buffer.flip();
                    if (buff.write(buffer) == 0) {
                        break;
                    }
                    buffer.clear();
                }
                return buff.array();
            } catch (IOException e) {
                logger.warning(e.getMessage());
            } finally {
                try {
                    channel.close();
                    stream.close();
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    /**
     * 计算数据的MD5值
     *
     * @param bytes 待计算字节流
     * @return MD5值
     */
    public String md5(byte[] bytes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(bytes);
            return new BigInteger(1, md5.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            logger.warning(e.getMessage());
        }
        return null;
    }

    /**
     * 使用gzip技术压缩数据
     *
     * @param bytes 待压缩字节流
     * @return 压缩后的字节流
     */
    public byte[] gzip(byte[] bytes) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(stream);
            gzip.write(bytes);
            gzip.close();
            return stream.toByteArray();
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return null;
    }
}
