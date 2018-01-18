package com.credits.service.contract.method;

import com.credits.exception.ContractExecutorException;

public class MethodParamStringValueRecognizer extends MethodParamValueRecognizer {

    private final char LITERAL_MARKER = '\"';

    public MethodParamStringValueRecognizer(String param) {
        super(param);
    }

    @Override
    public Object castValue(Class<?> type) throws ContractExecutorException {
        String retVal = MethodParamValueRecognizerHelper.createFromStringOrCharLiteral(param, LITERAL_MARKER);
        if (retVal == null) {
            throw new ContractExecutorException("Illegal string literal for the parameter");
        }

        if (String.class.equals(type)) {
            return retVal;
        } else {
            return type.cast(param);
        }
    }
}
