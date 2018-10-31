package com.credits.general.util.exception;


import com.credits.general.exception.CreditsException;

/**
 * Created by Igor Goryunov on 19.10.2018
 */
public class ConverterException extends CreditsException {

    public ConverterException(String errorMessage) {
        super(errorMessage);
    }

    public ConverterException(Exception e) {
        super(e);
    }
}
