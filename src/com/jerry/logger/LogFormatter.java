package com.jerry.logger;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 日志格式化规则
 */
public final class LogFormatter extends Formatter {

    /**
     * example:
     * [Tue Dec 03 22:55:52 CST 2019] [CONFIG]  [NIOConnector]
     * NIOConnector.start : Connector com.jerry.startup now.
     */
    @Override
    public String format(LogRecord record) {
        String date = new Date().toString();
        return "[" + date + "] [" +
                record.getLevel() + "] [" +
                record.getLoggerName() + "]\n" +
                record.getMessage() + "\n\n";
    }
}
