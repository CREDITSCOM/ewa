import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.contract.SmartContractConstants;
import com.credits.service.contract.session.InvokeMethodSession;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import com.credits.thrift.ReturnValue;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.credits.general.util.variant.VariantConverter.objectToVariantData;
import static com.credits.general.util.variant.VariantConverter.variantDataToVariant;
import static com.credits.general.util.variant.VariantConverter.variantToObject;
import static java.lang.Long.MAX_VALUE;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;

    private static NodeApiExecInteractionService nodeApiService;
    private static ContractExecutorServiceImpl contractExecutorService;
    private static ExecutorService cachedPool;
    private transient Map<String, ByteBuffer> externalContractsStates;

    protected final transient long accessId;
    protected final transient String initiator;
    protected final String contractAddress;

    public SmartContract() {
        SmartContractConstants contractConstants =
            SmartContractConstants.getSessionSmartContractConstants(Thread.currentThread().getId());
        initiator = contractConstants.initiator;
        accessId = contractConstants.accessId;
        contractAddress = contractConstants.contractAddress;
    }

    final protected void sendTransaction(String from, String to, double amount, double fee, byte... userData) {
        callService(() -> {
            nodeApiService.sendTransaction(accessId, from, to, amount, fee, userData);
            return null;
        });
    }

    final protected Object invokeExternalContract(String contractAddress, String method, Object... params) {
        SmartContractGetResultData contractData = callService(() -> nodeApiService.getExternalSmartContractByteCode(accessId, contractAddress));

        Variant[][] variantParams = null;
        if(params != null) {
            variantParams = new Variant[1][params.length];
            for (int i = 0; i < variantParams.length; i++) {
                variantParams[0][i] = variantDataToVariant(objectToVariantData(params[i]));
            }
        }

        final Variant[][] finalVariantParams = variantParams;
        final ReturnValue returnValue = callService(() -> contractExecutorService.executeExternalSmartContract(
            new InvokeMethodSession(
                accessId,
                initiator,
                contractAddress,
                contractData.byteCodeObjects,
                contractData.contractState,
                method,
                finalVariantParams,
                MAX_VALUE),
            externalContractsStates));

        if (!contractData.stateCanModify && !Arrays.equals(contractData.contractState, returnValue.newContractState)) {
            throw new ContractExecutorException("smart contract \"" + contractAddress + "\" can't be modify");
        }
        externalContractsStates.putIfAbsent(contractAddress, ByteBuffer.wrap(returnValue.newContractState));

        return variantToObject(returnValue.executeResults.get(0).result);
    }


    final protected byte[] getSeed() {
        return callService(() -> nodeApiService.getSeed(accessId));
    }

    private interface Function<R> {
        R apply();
    }

    private <R> R callService(SmartContract.Function<R> method) {
        try {
            return cachedPool.submit(method::apply).get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
