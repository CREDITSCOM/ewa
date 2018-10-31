package com.credits.exception;

public class ContractExecutorException extends Exception {
    public ContractExecutorException(String message, Throwable e) {
        super(message, e);
    }

    public ContractExecutorException(String message) {
        super(message);
    }
}
