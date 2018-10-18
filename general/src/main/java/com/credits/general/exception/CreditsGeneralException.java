package com.credits.general.exception;

/**
 * Created by Rustem.Saidaliyev on 28.06.2018.
 */
public class CreditsGeneralException extends CreditsException {

    public CreditsGeneralException(String errorMessage) {
        super(errorMessage);
    }

    public CreditsGeneralException(Exception e) {
        super(e);
    }
}
