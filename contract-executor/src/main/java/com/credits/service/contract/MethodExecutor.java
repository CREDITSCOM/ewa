package com.credits.service.contract;

import com.credits.general.thrift.generated.Variant;
import exception.ContractExecutorException;
import pojo.session.InvokeMethodSession;

import java.util.List;
import java.util.stream.Collectors;

import static com.credits.general.util.variant.VariantConverter.toVariant;
import static com.credits.utils.ContractExecutorServiceUtils.getMethodArgumentsValuesByNameAndParams;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

class MethodExecutor extends LimitedExecutionMethod<Variant> {
    private final InvokeMethodSession session;
    private final Object instance;
    private final ClassLoader classLoader;

    public MethodExecutor(InvokeMethodSession session, Object contractInstance) {
        super(session);
        this.session = session;
        this.instance = contractInstance;
        this.classLoader = instance.getClass().getClassLoader();
    }

    public List<MethodResult> execute() {
        return session.paramsTable.length < 2
                ? invokeSingleMethod()
                : invokeMultipleMethod();
    }

    public Object getSmartContractObject() {
        return instance;
    }

    private List<MethodResult> invokeSingleMethod() {
        return List.of(prepareResult(invoke(session.paramsTable[0])));
    }

    private List<MethodResult> invokeMultipleMethod() {
        return stream(session.paramsTable)
                .map(r -> prepareResult(invoke(r)))
                .collect(Collectors.toList());
    }

    private Variant invoke(Variant... params) {
        try {
            final var methodData = getMethodArgumentsValuesByNameAndParams(instance.getClass(), session.methodName, params, classLoader);
            final var method = methodData.method;
            final var returnTypeName = method.getReturnType().getTypeName();
            return runForLimitTime(() -> toVariant(returnTypeName, method.invoke(instance, methodData.argValues)));
        } catch (ClassNotFoundException e) {
            throw new ContractExecutorException(getRootCauseMessage(e));
        }
    }
}
