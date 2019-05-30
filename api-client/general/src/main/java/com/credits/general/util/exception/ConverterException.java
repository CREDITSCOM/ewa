package com.credits.general.util.exception;


import com.credits.general.exception.CreditsException;


public class ConverterException extends CreditsException {

    public ConverterException(String errorMessage) {
        super(errorMessage);
    }

    public ConverterException(Exception e) {
        super(e);
    }
}
