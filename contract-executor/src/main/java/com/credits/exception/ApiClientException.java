package com.credits.exception;


import com.credits.general.exception.CreditsException;


public class ApiClientException extends CreditsException {

    private static final long serialVersionUID = 4631697911096162136L;

    public ApiClientException(String errorMessage) {
        super(errorMessage);
    }

    public ApiClientException(Exception e) {
        super(e);
    }
}
