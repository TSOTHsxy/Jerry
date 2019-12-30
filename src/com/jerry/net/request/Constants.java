package com.jerry.net.request;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 常量池
 */
final class Constants {

    /**
     * 默认编码字符集
     */
    static final Charset charset = StandardCharsets.UTF_8;

    /**
     * 特殊字符集
     */
    static final char QUESTION = '?';
    static final char COLON    = ':';
    static final char CR       = '\r';
    static final char HT       = '\t';
    static final char SP       = ' ';
    static final char LF       = '\n';
    static final char A        = 'A';
    static final char Z        = 'Z';
}
