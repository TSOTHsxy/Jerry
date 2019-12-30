package com.jerry.agent.annotation;

import java.lang.annotation.*;

/**
 * 静态资源根目录注解
 * 将一个域值注解为静态资源根目录设置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface root {
}
