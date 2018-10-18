package com.credits.client.executor.exception;

import com.credits.general.exception.CreditsException;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public class ExecutorClientException extends CreditsException {

    public ExecutorClientException(String errorMessage) {
        super(errorMessage);
    }

    public ExecutorClientException(Exception e) {
        super(e);
    }
}
