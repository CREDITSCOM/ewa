package com.credits.service.contract.method;

import com.credits.exception.ContractExecutorException;

public class MethodParamCharValueRecognizer extends MethodParamValueRecognizer {
    private final char LITERAL_MARKER = '\'';

    public MethodParamCharValueRecognizer(String param) {
        super(param);
    }

    @Override
    public Object castValue(Class<?> type) throws ContractExecutorException {
        String retVal = MethodParamValueRecognizerHelper.createFromStringOrCharLiteral(param, LITERAL_MARKER);
        if (retVal == null) {
            throw new ContractExecutorException("Illegal string literal for the parameter");
        }

        if (type.isPrimitive() && Character.TYPE.equals(type)) {
            return retVal.charAt(0);
        } else {
            return type.cast(param);
        }
    }
}
