package com.credits.service.contract.method;

import com.credits.exception.ContractExecutorException;
import org.apache.commons.lang3.math.NumberUtils;
import static com.credits.service.contract.method.MethodParamValueRecognizerHelper.*;

public class MethodParamNumberValueRecognizer extends MethodParamValueRecognizer {
    public MethodParamNumberValueRecognizer(String param) {
        super(param);
    }

    @Override
    public Object castValue(Class<?> type) throws ContractExecutorException {
        Object retVal = null;
        Number numParam;
        if (isCastedNumber(param)) {
            numParam = createCastedNumber(param);
            if (numParam == null) {
                throw new ContractExecutorException("Unknown primitive type of the parameter");
            }
        } else {
            if (isDoubleLiteral(param)) {
                numParam = NumberUtils.createDouble(param);
            } else {
                numParam = NumberUtils.createNumber(param);
            }
        }

        if (Long.class.equals(numParam.getClass())) {
            if (type.isPrimitive() && Long.TYPE.equals(type)) {
                retVal = numParam.longValue();
            } else {
                retVal = type.cast(numParam.longValue());
            }
        } else if (Integer.class.equals(numParam.getClass())) {
            if (type.isPrimitive() && Integer.TYPE.equals(type)) {
                retVal = numParam.intValue();
            } else {
                retVal = type.cast(numParam.intValue());
            }
        } else if (Double.class.equals(numParam.getClass())) {
            if (type.isPrimitive() && Double.TYPE.equals(type)) {
                retVal = numParam.doubleValue();
            } else {
                retVal = type.cast(numParam.doubleValue());
            }
        } else if (Float.class.equals(numParam.getClass())) {
            if (type.isPrimitive() && Float.TYPE.equals(type)) {
                retVal = numParam.floatValue();
            } else {
                retVal = type.cast(numParam.floatValue());
            }
        } else if (Short.class.equals(numParam.getClass())) {
            if (type.isPrimitive() && Short.TYPE.equals(type)) {
                retVal = numParam.shortValue();
            } else {
                retVal = type.cast(numParam.shortValue());
            }
        } else if (Byte.class.equals(numParam.getClass())) {
            if (type.isPrimitive() && Byte.TYPE.equals(type)) {
                retVal = numParam.byteValue();
            } else {
                retVal = type.cast(numParam.byteValue());
            }
        }
        return retVal;
    }
}
