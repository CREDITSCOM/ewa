package com.credits.service.contract.method;

import com.credits.exception.ContractExecutorException;

import java.lang.reflect.Array;

public class MethodParamArrayRecognizer extends MethodParamValueRecognizer {
    public MethodParamArrayRecognizer(String param) {
        super(param);
    }

    @Override
    public Object castValue(Class<?> typeOfArray) throws ContractExecutorException {
        String paramArray = param;
        int firstBrakePos = paramArray.indexOf('{');
        int lastBrakePos = paramArray.lastIndexOf('}');

        if (firstBrakePos == -1 || lastBrakePos == -1) {
            throw new ContractExecutorException("Illegal array representation");
        }

        paramArray = paramArray.substring(firstBrakePos + 1, lastBrakePos);
        String[] elems = paramArray.split(",");
        Object retVal = Array.newInstance(typeOfArray, elems.length);

        int i = 0;
        for (String elem : elems) {
            elem = elem.trim();
            MethodParamValueRecognizer recognizer = MethodParamValueRecognizerFactory.get(elem);
            Object valueCasted = recognizer.castValue(typeOfArray);
            Array.set(retVal, i++, valueCasted);
        }
        return retVal;
    }
}
