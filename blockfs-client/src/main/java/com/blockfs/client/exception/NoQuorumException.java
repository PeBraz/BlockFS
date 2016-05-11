package com.blockfs.client.exception;

public class NoQuorumException extends RuntimeException {
    public NoQuorumException(String message) {
        super(message);
    }
}
