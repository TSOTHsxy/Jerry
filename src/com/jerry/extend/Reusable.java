package com.jerry.extend;

import com.jerry.utils.CacheQueue;

/**
 * 对象可复用抽象接口
 * 主要和容器类（比如{@link CacheQueue}）搭配来减少新建对象的开支
 * 实际上泛型无法直接作用在基本类型上大大削弱了它的作用
 *
 * @param <T> 可复用对象的变化部分的类型
 */
public interface Reusable<T> {

    /**
     * 设置新的数据复用对象
     *
     * @param obj 可复用对象的变化部分
     */
    void reset(T obj);

    /**
     * 清理可复用对象旧的上下文
     */
    void clear();
}
