package com.jerry.logger;

import java.util.logging.Formatter;
import java.util.logging.Level;

/**
 * 日志样式接口默认实现
 */
public class SimpleLogStyle implements LogStyle {
    @Override
    public Level level() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public Formatter format() {
        return null;
    }
}
