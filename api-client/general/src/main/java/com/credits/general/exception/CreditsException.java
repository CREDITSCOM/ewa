package com.credits.general.exception;


public class CreditsException extends RuntimeException {

    public CreditsException(String errorMessage) {
        super(errorMessage);
    }

    public CreditsException(Exception e) {
        super(e);
    }

    public CreditsException(String errorMessage, Throwable e) {
        super(e);
    }

}
