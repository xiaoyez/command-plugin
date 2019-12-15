package com.xiaoye.command.core;

public class CommandStrParseException extends RuntimeException {
    public CommandStrParseException() {
    }

    public CommandStrParseException(String message) {
        super(message);
    }

    public CommandStrParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandStrParseException(Throwable cause) {
        super(cause);
    }

    public CommandStrParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
