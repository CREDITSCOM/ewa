package com.credits.client.node.exception;


import com.credits.general.exception.CreditsException;

/**
 * Created by Rustem.Saidaliyev on 28.06.2018.
 */
public class CreditsNodeException extends CreditsException {

    public CreditsNodeException(String errorMessage) {
        super(errorMessage);
    }

    public CreditsNodeException(Exception e) {
        super(e);
    }
}
