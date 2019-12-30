package com.jerry.extend;

import java.nio.charset.Charset;

/**
 * 编码字符集设置抽象接口
 */
public interface CodeAble {

    /**
     * 设置编码字符集
     * 在此项目中仅服务于{@link com.jerry.net.request.Request}类及其子类
     *
     * @param charset 编码字符集
     */
    void setCharset(Charset charset);
}
