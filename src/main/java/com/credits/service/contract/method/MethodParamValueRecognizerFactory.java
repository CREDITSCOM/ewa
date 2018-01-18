package com.credits.service.contract.method;

import com.credits.exception.ContractExecutorException;

import static com.credits.service.contract.method.MethodParamValueRecognizerHelper.*;

public class MethodParamValueRecognizerFactory {
    public static MethodParamValueRecognizer get(String value) throws ContractExecutorException {
        MethodParamValueRecognizer recognizer;
        if (value == null || isNullLiteral(value)) {
            recognizer = new MethodParamNullValueRecognizer(value);
        } else if (isNumberLiteralOrCastableNumber(value)) {
            recognizer = new MethodParamNumberValueRecognizer(value);
        } else if (isStringLiteral(value)) {
            recognizer = new MethodParamStringValueRecognizer(value);
        } else if (isCharLiteral(value)) {
            recognizer = new MethodParamCharValueRecognizer(value);
        } else if (isBooleanLiteral(value)) {
            recognizer = new MethodParamBooleanValueRecognizer(value);
        } else if (isArray(value)) {
            recognizer = new MethodParamArrayRecognizer(value);
        } else {
            throw new ContractExecutorException("Unknown literal for the parameter");
        }

        return recognizer;
    }
}
