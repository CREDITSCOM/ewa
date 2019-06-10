package com.credits.service.contract;

import com.credits.general.thrift.generated.Variant;

import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

class MethodResult {
    private final long spentCpuTime;
    private final Variant returnValue;
    private final Throwable exception;
    private Object invokedObject;

    public MethodResult(Variant returnValue, long spentCpuTime) {
        this.spentCpuTime = spentCpuTime;
        this.returnValue = returnValue;
        exception = null;
    }

    public MethodResult(Throwable exception, long spentCpuTime) {
        this.spentCpuTime = spentCpuTime;
        this.exception = exception;

        returnValue = new Variant(V_STRING, getRootCauseMessage(exception));
    }

    public long getSpentCpuTime() {
        return spentCpuTime;
    }

    public Variant getReturnValue() {
        return returnValue;
    }

    public Throwable getException() {
        return exception;
    }

    public Object getInvokedObject() {
        return invokedObject;
    }

    public void setInvokedObject(Object invokedObject) {
        this.invokedObject = invokedObject;
    }

}
