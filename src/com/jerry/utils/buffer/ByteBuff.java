package com.jerry.utils.buffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 动态扩容缓冲区默认实现
 * 通过整体拷贝的方式对缓冲区进行扩容
 * 参考Netty中ByteBuff类的实现
 */
public final class ByteBuff extends AbstractBuff {

    /**
     * 缓冲区底层数组
     */
    private byte[] buffer;

    /**
     * @param maxCapacity 期望的缓冲区最大容量
     * @param capacity    期望的缓冲区容量
     */
    public ByteBuff(int maxCapacity, int capacity) {
        super(maxCapacity, capacity);
        buffer = new byte[capacity];
    }

    public ByteBuff() {
        this(DEFAULT_MAX_CAPACITY, DEFAULT_CAPACITY);
    }

    @Override
    public int write(ByteBuffer byteBuffer) {
        return write(byteBuffer.array(), byteBuffer.arrayOffset()
                + byteBuffer.position(), byteBuffer.remaining());
    }

    @Override
    public byte[] array() {
        byte[] bytes = new byte[position()];
        System.arraycopy(buffer, 0, bytes, 0, position());
        return bytes;
    }

    /**
     * 从源字节流中读取字节并写入缓冲区
     */
    private int write(byte[] bytes, int offset, int length) {
        if (offset < 0 || length < 0 || offset > bytes.length || length > bytes.length) {
            throw new IllegalArgumentException(String.format("offset: %d; " +
                    "length: %d; Parameter value error.", offset, length));
        }
        ensureWritable(length);
        if (writableBytes() == 0) {
            return 0;
        }
        int len = Math.min(maxCapacity(), position() + length) - position();
        System.arraycopy(bytes, offset, buffer, position(), len);
        position(position() + len);
        return len;
    }

    /**
     * 检查并动态扩展缓冲区容量
     */
    private void ensureWritable(int length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                    String.format("length: %d (expected > 0)", length));
        }
        if (length < writableBytes()) {
            return;
        }
        int newCapacity = calculateNewCapacity(position() + length);
        allocationCapacity(newCapacity);
    }

    /**
     * 计算新容量
     * 如果满足本次写操作的最小容量小于扩容阈值则按步进的方式扩容
     * 否则则以扩容阈值的值作为增量扩容
     */
    private int calculateNewCapacity(int minWritableBytes) {
        if (minWritableBytes == INCREMENT) {
            return minWritableBytes;
        }
        //以扩容阈值的值作为增量扩容
        if (minWritableBytes > INCREMENT) {
            int newWritableBytes = minWritableBytes / INCREMENT * INCREMENT;
            if (newWritableBytes > maxCapacity() - INCREMENT) {
                newWritableBytes = maxCapacity();
            } else {
                newWritableBytes += INCREMENT;
            }
            return newWritableBytes;
        }
        //以步进的方式扩容
        int newWritableBytes = 64;
        while (newWritableBytes < minWritableBytes) {
            newWritableBytes <<= 1;
        }
        return Math.min(newWritableBytes, maxCapacity());
    }

    private void allocationCapacity(int newCapacity) {
        if (newCapacity < 0 || newCapacity > maxCapacity()) {
            throw new IllegalArgumentException(
                    String.format("newCapacity: %d (expected > 0)", maxCapacity()));
        }
        if (newCapacity != capacity()) {
            byte[] newArray = new byte[newCapacity];
            System.arraycopy(buffer, 0, newArray, 0, position());
            buffer = newArray;
            capacity(newCapacity);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(buffer);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteBuff) {
            ByteBuff buf = (ByteBuff) obj;
            return Arrays.equals(buffer, buf.buffer);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "( capacity: " + capacity() + " , " +
                "maxCapacity: " + maxCapacity() + " )";
    }
}
