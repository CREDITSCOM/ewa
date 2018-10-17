package com.credits.client.crypto.exception;


import com.credits.client.exception.CreditsException;

/**
 * Created by Rustem.Saidaliyev on 27.03.2018.
 */
public class CreditsCryptoException extends CreditsException {

    public CreditsCryptoException(String errorMessage) {
        super(errorMessage);
    }

    public CreditsCryptoException(Exception e) {
        super(e);
    }
}
