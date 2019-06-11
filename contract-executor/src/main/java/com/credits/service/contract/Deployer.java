package com.credits.service.contract;

import com.credits.general.thrift.generated.Variant;
import pojo.session.DeployContractSession;

import static com.credits.general.thrift.generated.Variant._Fields.V_VOID;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static com.credits.thrift.utils.ContractExecutorUtils.checkThatIsNotCreditsToken;

class Deployer extends LimitedExecutionMethod<Object> {

    private Class<?> contractClass;

    public Deployer(DeployContractSession session, Class<?> contractClass) {
        super(session);
        this.contractClass = contractClass;
    }

    public MethodResult deploy() {
        final Object instance = runForLimitTime(() -> contractClass.getDeclaredConstructor().newInstance());
        checkThatIsNotCreditsToken(contractClass, instance);
        final var deployResult = prepareResult(new Variant(V_VOID, VOID_TYPE_VALUE));
        deployResult.setInvokedObject(instance);
        return deployResult;
    }
}
