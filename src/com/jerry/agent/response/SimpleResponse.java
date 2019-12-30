package com.jerry.agent.response;

/**
 * HTTP协议响应抽象实现
 * 为支持责任链处理模式扩展了新的接口
 */
abstract class SimpleResponse implements Response {

    /**
     * 请求处理状态
     */
    private State state;

    public SimpleResponse() {
        state = State.UNREADY;
    }

    /**
     * 设置请求处理状态
     *
     * @param state 请求处理状态
     */
    public void state(State state) {
        this.state = state;
    }

    /**
     * @return 请求处理状态
     */
    public State state() {
        return state;
    }
}
