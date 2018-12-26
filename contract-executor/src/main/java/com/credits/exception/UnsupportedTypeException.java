package com.credits.exception;

public class UnsupportedTypeException extends Exception {
    public UnsupportedTypeException() {
    }

    public UnsupportedTypeException(String message) {
        super(message);
    }

    public UnsupportedTypeException(String message, Throwable e) {
        super(message, e);
    }
}
