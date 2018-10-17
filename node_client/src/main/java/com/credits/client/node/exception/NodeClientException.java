package com.credits.client.node.exception;


import com.credits.client.exception.CreditsException;

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
