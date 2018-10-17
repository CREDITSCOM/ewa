package com.credits.general.client.node.exception;


import com.credits.general.exception.CreditsException;

/**
 * Created by Rustem.Saidaliyev on 28.06.2018.
 */
public class NodeClientException extends CreditsException {

    public NodeClientException(String errorMessage) {
        super(errorMessage);
    }

    public NodeClientException(Exception e) {
        super(e);
    }
}
