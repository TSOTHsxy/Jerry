package com.jerry.logger;

import java.util.logging.Formatter;
import java.util.logging.Level;

/**
 * 日志样式抽象接口
 * 建议使用匿名内部类设置日志样式
 */
public interface LogStyle {

    /**
     * @return 日志过滤等级
     * 返回{@code null}表示忽略此项设置
     */
    Level level();

    /**
     * @return 日志保存路径
     * 如 ./Jerry.log
     * 返回{@code null}表示忽略此项设置
     */
    String path();

    /**
     * @return 日志启用状态
     * 返回{@code true}表示禁用日志记录
     */
    boolean disabled();

    /**
     * @return 日志格式化规则
     * 建议返回{@code Formatter}类的匿名内部类
     * 返回{@code null}表示忽略此项设置
     */
    Formatter format();
}
