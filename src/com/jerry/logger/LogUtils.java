package com.jerry.logger;

import java.io.IOException;
import java.util.logging.*;

/**
 * 日志记录工具类
 */
public final class LogUtils {

    /**
     * 初始化日志记录器
     *
     * @param logger   日志记录器
     * @param logStyle 日志样式
     */
    public static void initLogger(Logger logger, LogStyle logStyle) {
        if (logStyle.disabled()) {
            logger.setLevel(Level.OFF);
            return;
        }
        Level level = logStyle.level();
        if (level != null) {
            logger.setLevel(level);
        }
        Formatter formatter = logStyle.format();
        if (formatter != null) {
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(formatter);
            ch.setLevel(logger.getLevel());
            logger.addHandler(ch);
            logger.setUseParentHandlers(false);
        }
        String path = logStyle.path();
        if (path != null) {
            try {
                FileHandler fh = new FileHandler(path, 1000, 2, true);
                fh.setLevel(logger.getLevel());
                if (formatter != null) {
                    fh.setFormatter(formatter);
                }
                logger.addHandler(fh);
            } catch (IOException ignored) {
            }
        }
    }
}
