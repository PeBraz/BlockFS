package com.blockfs.client;

public class ServerRespondedErrorException extends Exception {

    public ServerRespondedErrorException() {
    }

    public ServerRespondedErrorException(String message) {
        super(message);
    }

    public ServerRespondedErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerRespondedErrorException(Throwable cause) {
        super(cause);
    }

    public ServerRespondedErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
