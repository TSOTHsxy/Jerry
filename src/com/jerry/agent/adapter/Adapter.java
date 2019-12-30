package com.jerry.agent.adapter;

/**
 * 数据适配器抽象接口
 * 提供从POJO到网络传输时的字节流的转换
 * 用于支持映射对象中映射方法的多样化返回类型
 *
 * @param <T> 需要转化为字节流的对象类型
 */
public interface Adapter<T> {

    /**
     * 将普通对象转化为字节流
     *
     * @param obj 需要转化为字节流的对象
     * @return 转化后的字节流
     */
    byte[] toBytes(T obj);
}
