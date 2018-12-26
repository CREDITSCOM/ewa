package com.credits.general.crypto.exception;


import com.credits.general.exception.CreditsException;

/**
 * Created by Rustem.Saidaliyev on 27.03.2018.
 */
public class CryptoException extends CreditsException {

    public CryptoException(String errorMessage) {
        super(errorMessage);
    }

    public CryptoException(Exception e) {
        super(e);
    }
}
