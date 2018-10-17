package com.credits.general.exception;

/**
 * Created by Rustem.Saidaliyev on 28.06.2018.
 */
public class CreditsCommonException extends CreditsException {

    public CreditsCommonException(String errorMessage) {
        super(errorMessage);
    }

    public CreditsCommonException(Exception e) {
        super(e);
    }
}
