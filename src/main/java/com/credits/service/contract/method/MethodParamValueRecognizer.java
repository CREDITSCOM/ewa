package com.credits.service.contract.method;

import com.credits.exception.ContractExecutorException;

public abstract class MethodParamValueRecognizer {

    protected String param;

    public MethodParamValueRecognizer(String param) {
        this.param = param;
    }

    public abstract Object castValue(Class<?> type) throws ContractExecutorException;
}
