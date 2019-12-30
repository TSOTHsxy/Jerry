package com.jerry.net.request;

/**
 * Socket会话请求抽象实现
 */
public abstract class SimpleRequest implements Request {

    /**
     * 请求解析状态
     */
    protected boolean state;

    /**
     * 请求标识
     */
    private String addr;

    /**
     * 请求地址
     */
    protected String url;

    @Override
    public boolean failedParsing() {
        return !state;
    }

    @Override
    public void addr(String addr) {
        this.addr = addr;
    }

    @Override
    public String addr() {
        return addr;
    }

    @Override
    public String url() {
        if (failedParsing()) {
            throw new IllegalOperationException();
        }
        return url;
    }
}
