package com.credits.general.exception;

import java.io.UnsupportedEncodingException;

public class CompilationException extends CreditsException {

    public CompilationException(String message) {
        super(message);
    }

    public CompilationException(UnsupportedEncodingException e) {
        super(e);
    }
}
