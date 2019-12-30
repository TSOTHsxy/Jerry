package com.jerry.extend;

/**
 * 组件生命周期抽象接口
 */
public interface Lifecycle {

    /**
     * 启动组件
     *
     * @return 启动状态
     */
    boolean start();

    /**
     * 停止组件
     */
    void stop();
}
