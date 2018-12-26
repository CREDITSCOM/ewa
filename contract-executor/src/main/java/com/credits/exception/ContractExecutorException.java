package com.credits.exception;

import com.credits.general.exception.CreditsException;

public class ContractExecutorException extends CreditsException {
    public ContractExecutorException(String message, Throwable e) {
        super(message, e);
    }

    public ContractExecutorException(String message) {
        super(message);
    }
}
