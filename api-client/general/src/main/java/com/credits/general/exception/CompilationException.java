package com.credits.general.exception;

public class CompilationException extends Exception {
    public CompilationException(String message, Throwable e) {
        super(message, e);
    }

    public CompilationException(String message) {
        super(message);
    }

    public CompilationException(Throwable e) { super(e); }
}
