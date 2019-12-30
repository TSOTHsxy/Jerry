package com.jerry.utils.buffer;

import java.nio.ByteBuffer;

/**
 * 动态扩容缓冲区抽象实现
 * 参考{@link java.nio.Buffer}的实现
 */
abstract class AbstractBuff {

    /**
     * 默认缓冲区最大容量
     */
    static final int DEFAULT_MAX_CAPACITY = 1048576 * 24;    //default 24M

    /**
     * 默认缓冲区容量
     */
    static final int DEFAULT_CAPACITY = 1024 * 512;          //default 512K

    /**
     * 扩容阈值
     * 控制动态扩容的容量增量
     */
    static final int INCREMENT = 1024 * 1024;                //default 4M

    /**
     * 写指针偏移量
     */
    private int position = 0;

    /**
     * 缓冲区最大容量
     */
    private int maxCapacity;

    /**
     * 缓冲区容量
     */
    private int capacity;

    /**
     * @param maxCapacity 期望的缓冲区最大容量
     * @param capacity    期望的缓冲区容量
     */
    AbstractBuff(int maxCapacity, int capacity) {
        if (maxCapacity <= 0 || capacity > maxCapacity) {
            throw new IllegalArgumentException("Illegal argument.");
        }
        this.maxCapacity = maxCapacity;
        this.capacity = capacity;
    }

    /**
     * @return 缓冲区最大容量
     */
    final int maxCapacity() {
        return maxCapacity;
    }

    /**
     * 设置缓冲区容量
     *
     * @param newCapacity 缓冲区容量
     */
    final void capacity(int newCapacity) {
        if (newCapacity < 0 || newCapacity > maxCapacity) {
            throw new IllegalArgumentException(String.format("newCapacity: " +
                    "%d (expected > 0 && < maxCapacity)", newCapacity));
        }
        this.capacity = newCapacity;
    }

    /**
     * @return 缓冲区容量
     */
    final int capacity() {
        return capacity;
    }

    /**
     * 设置写指针偏移量
     *
     * @param newPosition 写指针偏移量
     */
    final void position(int newPosition) {
        if (newPosition < 0 || newPosition > capacity) {
            throw new IllegalArgumentException(String.format("newPosition:" +
                    " %d (expected > 0 && < capacity)", newPosition));
        }
        this.position = newPosition;
    }

    /**
     * @return 写指针偏移量
     */
    final int position() {
        return position;
    }

    /**
     * @return 缓冲区可写字节数
     */
    final int writableBytes() {
        return capacity - position;
    }

    /**
     * @return 缓冲区状态
     * 返回{@code ture}表示缓冲区为空
     */
    final public boolean isEmpty() {
        return position == 0;
    }

    /**
     * 清空缓冲区
     */
    final public void clear() {
        position(0);
    }

    /**
     * 从源字节缓冲区中读取字节并写入缓冲区
     *
     * @param buffer 源字节缓冲区
     * @return 写入的字节数
     */
    abstract public int write(ByteBuffer buffer);

    /**
     * @return 缓冲区已写字节的副本
     */
    abstract public byte[] array();
}
