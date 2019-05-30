package com.credits.client.node.exception;


import com.credits.general.exception.CreditsException;


public class NodeClientException extends CreditsException {

    public NodeClientException(String errorMessage) {
        super(errorMessage);
    }

    public NodeClientException(Exception e) {
        super(e);
    }
}
