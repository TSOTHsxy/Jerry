package com.jerry.agent.adapter;

import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * 默认数据适配器映射表
 */
public final class DefaultAdapters extends HashMap<Class<?>, Adapter<?>> {

    public DefaultAdapters() {
        //添加基本类型适配器
        put(Long.class, (Adapter<Long>) obj -> new byte[]{obj.byteValue()});
        put(Integer.class, (Adapter<Integer>) obj -> new byte[]{obj.byteValue()});
        put(Short.class, (Adapter<Short>) obj -> new byte[]{obj.byteValue()});
        put(Double.class, (Adapter<Double>) obj -> new byte[]{obj.byteValue()});
        put(Float.class, (Adapter<Float>) obj -> new byte[]{obj.byteValue()});
        put(Boolean.class, (Adapter<Boolean>) obj -> obj.toString().getBytes());

        //添加数组类型适配器
        put(byte[].class, (Adapter<byte[]>) obj -> obj);
        put(long[].class, (Adapter<long[]>) obj -> {
            ByteBuffer bf = ByteBuffer.allocate(obj.length * 8);
            bf.order(ByteOrder.LITTLE_ENDIAN);
            bf.asLongBuffer().put(obj);
            return bf.array();
        });
        put(int[].class, (Adapter<int[]>) obj -> {
            ByteBuffer bf = ByteBuffer.allocate(obj.length * 4);
            bf.order(ByteOrder.LITTLE_ENDIAN);
            bf.asIntBuffer().put(obj);
            return bf.array();
        });
        put(short[].class, (Adapter<short[]>) obj -> {
            ByteBuffer bf = ByteBuffer.allocate(obj.length * 2);
            bf.order(ByteOrder.LITTLE_ENDIAN);
            bf.asShortBuffer().put(obj);
            return bf.array();
        });
        put(double[].class, (Adapter<double[]>) obj -> {
            ByteBuffer bf = ByteBuffer.allocate(obj.length * 8);
            bf.order(ByteOrder.LITTLE_ENDIAN);
            bf.asDoubleBuffer().put(obj);
            return bf.array();
        });
        put(float[].class, (Adapter<float[]>) obj -> {
            ByteBuffer bf = ByteBuffer.allocate(obj.length * 4);
            bf.order(ByteOrder.LITTLE_ENDIAN);
            bf.asFloatBuffer().put(obj);
            return bf.array();
        });
        put(char[].class, (Adapter<char[]>) obj -> {
            Charset cs = StandardCharsets.UTF_8;
            CharBuffer cb = CharBuffer.wrap(obj);
            ByteBuffer bb = cs.encode(cb);
            return bb.array();
        });

        //添加对象类型适配器
        put(String.class, (Adapter<String>) String::getBytes);
    }
}
