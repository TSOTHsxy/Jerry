package com.jerry.net.request;

/**
 * 非法操作异常
 */
public final class IllegalOperationException extends RuntimeException {

    public IllegalOperationException() {
        super("Unable to operate on request with resolution failure.");
    }
}
