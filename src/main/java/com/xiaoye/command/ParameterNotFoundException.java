package com.xiaoye.command;

public class ParameterNotFoundException extends RuntimeException {

    public ParameterNotFoundException() {
    }

    public ParameterNotFoundException(String message) {
        super(message);
    }

    public ParameterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterNotFoundException(Throwable cause) {
        super(cause);
    }

    public ParameterNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
