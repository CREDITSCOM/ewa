package com.credits.general.crypto.exception;


import com.credits.general.exception.CreditsException;


public class CryptoException extends CreditsException {

    public CryptoException(String errorMessage) {
        super(errorMessage);
    }

    public CryptoException(Exception e) {
        super(e);
    }
}
