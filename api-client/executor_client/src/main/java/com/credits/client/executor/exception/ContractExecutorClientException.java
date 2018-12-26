package com.credits.client.executor.exception;

import com.credits.general.exception.CreditsException;

/**
 * Created by Igor Goryunov on 18.10.2018
 */
public class ContractExecutorClientException extends CreditsException {

    public ContractExecutorClientException(String errorMessage) {
        super(errorMessage);
    }

    public ContractExecutorClientException(Exception e) {
        super(e);
    }
}
