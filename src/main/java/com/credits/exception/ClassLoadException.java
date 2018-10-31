package com.credits.exception;

public class ClassLoadException extends Exception {

    public ClassLoadException(String message, Throwable e) {
        super(message, e);
    }

    public ClassLoadException(String message) {
        super(message);
    }
}
