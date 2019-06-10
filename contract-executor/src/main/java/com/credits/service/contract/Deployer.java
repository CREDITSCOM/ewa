package com.credits.service.contract;

import com.credits.general.thrift.generated.Variant;
import exception.ContractExecutorException;
import pojo.ReturnValue;
import pojo.SmartContractMethodResult;
import pojo.session.DeployContractSession;

import static com.credits.general.serialize.Serializer.serialize;
import static com.credits.general.thrift.generated.Variant._Fields.V_VOID;
import static com.credits.general.util.variant.VariantConverter.VOID_TYPE_VALUE;
import static com.credits.thrift.utils.ContractExecutorUtils.checkThatIsNotCreditsToken;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static java.util.Collections.singletonList;

class Deployer extends LimitedExecutionMethod<Object> {

    private Class<?> contractClass;

    public Deployer(DeployContractSession session, Class<?> contractClass) {
        super(session);
        this.contractClass = contractClass;
    }

    public ReturnValue deploy() {
        final Object instance = runForLimitTime(() -> contractClass.getDeclaredConstructor().newInstance());
        if (getExceptionOrNull() != null) throw new ContractExecutorException(getExceptionOrNull().getMessage());
        checkThatIsNotCreditsToken(contractClass, instance);
        return new ReturnValue(serialize(instance), singletonList(
                new SmartContractMethodResult(SUCCESS_API_RESPONSE,
                                              new Variant(V_VOID, VOID_TYPE_VALUE),
                                              spentCpuTime())), session.usedContracts);
    }
}
