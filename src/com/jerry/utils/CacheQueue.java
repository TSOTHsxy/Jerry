package com.jerry.utils;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 使用Java引用特性实现的高速缓存队列
 * 通过{@link Reference}对象来间接持有缓存对象
 * 支持在时间开销和空间开销中权衡
 * <p>
 * Java的引用分为四种：
 * 强引用：一般的对象引用
 * 软引用：{@link SoftReference}
 * 弱引用：{@link WeakReference}
 * 虚引用：{@link java.lang.ref.PhantomReference}
 *
 * @param <T> 容器间接持有的对象的类型
 */
public final class CacheQueue<T> {

    /**
     * 实际持有的引用对象的线程安全队列
     * 通过持有引用对象达到间接持有缓存对象的目的
     */
    private Queue<Reference<T>> cacheQueue = new ConcurrentLinkedQueue<>();

    /**
     * 与引用对象相关联的引用队列
     * 引用对象所引用的对象被回收时会反映到该队列中
     */
    private ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();

    /**
     * 使用模式
     * 为{@code true}表示使用弱引用模式
     * 为{@code false}表示使用软引用模式
     */
    private boolean isWeak;

    /**
     * @param isWeak 使用模式
     */
    public CacheQueue(boolean isWeak) {
        this.isWeak = isWeak;
    }

    public CacheQueue() {
        this(false);
    }

    /**
     * 将对象置入缓存队列
     *
     * @param cache 待缓存对象
     */
    public void cache(T cache) {
        invalid();
        cacheQueue.offer(reference(cache));
    }

    /**
     * @return 缓存对象
     */
    public T get() {
        Reference<T> reference;
        T cache;
        while ((reference = cacheQueue.poll()) != null) {
            if ((cache = reference.get()) != null) {
                return cache;
            }
        }
        return null;
    }

    /**
     * 清空缓存队列
     */
    public void clear() {
        invalid();
        cacheQueue.clear();
    }

    /**
     * 建立对待缓存对象的间接引用
     */
    private Reference<T> reference(T cache) {
        if (isWeak) {
            return new WeakReference<>(cache);
        } else {
            return new SoftReference<>(cache);
        }
    }

    /**
     * 清理失效的引用对象
     */
    @SuppressWarnings("unchecked")
    private void invalid() {
        Reference<T> reference;
        while ((reference = (Reference<T>) referenceQueue.poll()) != null) {
            cacheQueue.remove(reference);
        }
    }

    @Override
    public int hashCode() {
        return cacheQueue.hashCode() ^ referenceQueue.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CacheQueue) {
            CacheQueue cq = (CacheQueue) obj;
            return cacheQueue.equals(cq.cacheQueue)
                    && referenceQueue.equals(cq.referenceQueue);
        }
        return false;
    }
}
