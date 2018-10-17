package com.credits.general.exception;

/**
 * Created by Rustem.Saidaliyev on 27.03.2018.
 */
public class CreditsException extends Exception {

    public CreditsException(String errorMessage) {
        super(errorMessage);
    }

    public CreditsException(Exception e) {
        super(e);
    }
}
