package com.jerry.agent.annotation;

import com.jerry.agent.valve.MapperValve;

import java.lang.annotation.*;

/**
 * 请求映射注解
 * 将请求映射为对被注解方法的调用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface url {

    /**
     * @return 请求地址
     * 主要服务于{@link MapperValve}
     * 通过反射建立请求映射从而模仿Python Flask框架的使用形式
     */
    String value();

    /**
     * @return 响应资源类型
     * 主要服务于HTTP协议响应报文的{@code Content-Type}字段
     */
    String type() default "text/html";

    /**
     * @return 授权的请求方法类型
     * 限制客户端请求的方式
     */
    String method() default "all";
}
