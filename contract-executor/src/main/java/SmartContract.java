import com.credits.exception.ContractExecutorException;
import com.credits.general.exception.CreditsException;
import com.credits.pojo.apiexec.SmartContractGetResultData;
import com.credits.service.contract.ContractExecutorServiceImpl;
import com.credits.service.node.apiexec.NodeApiExecInteractionService;
import com.credits.thrift.ReturnValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.credits.general.util.variant.VariantConverter.variantToObject;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;

    protected static NodeApiExecInteractionService service;
    protected static ContractExecutorServiceImpl contractExecutorService;
    protected List<byte[]> externalContractsStateByteCode = new ArrayList<>();
    protected static ExecutorService cachedPool;
    protected static transient long accessId = 0;
    protected static transient String initiator;
    protected static String contractAddress = "";
    //    final protected BigDecimal getBalance(String address) {
    //        return callService(() -> {
    //            byte currencyByte = (byte) 1;
    //            return service.getBalance(address, currencyByte);
    //        });
    //    }
    //
    //    final protected TransactionData getTransaction(String transactionId) {
    //        return callService(() -> service.getTransaction(transactionId));
    //    }
    //
    //    final protected List<TransactionData> getTransactions(String address, long offset, long limit) {
    //        return callService(() -> service.getTransactions(address, offset, limit));
    //    }
    //
    //    final protected List<PoolData> getPoolList(long offset, long limit) {
    //        return callService(() -> service.getPoolList(offset, limit));
    //    }
    //
    //    final protected PoolData getPoolInfo(byte[] hash, long index) {
    //        return callService(() -> service.getPoolInfo(hash, index));
    //    }

    final protected void sendTransaction(String target, double amount, double fee, byte[] userData) {
        callService(() -> {
            service.sendTransaction(contractAddress, target, amount, fee, userData);
            return null;
        });
    }

    final protected Object invokeExternalContract(String externalSmartContractAddress, String externalSmartContractMethod, List externalSmartContractParams) {
        SmartContractGetResultData externalSmartContractByteCode =
            callService(() -> service.getExternalSmartContractByteCode(accessId, externalSmartContractAddress));

        ReturnValue returnValue = contractExecutorService.executeExternalSmartContract(accessId, initiator, externalSmartContractAddress,
                externalSmartContractMethod, externalSmartContractParams, externalSmartContractByteCode);
        if(externalSmartContractByteCode.getContractState()!=returnValue.getContractState() && !externalSmartContractByteCode.isStateCanModify()) {
            throw new ContractExecutorException("Contract state can not be modify");
        }
        this.externalContractsStateByteCode.add(returnValue.getContractState());
        return variantToObject(returnValue.getVariantsList().get(0));
    }


    final protected byte[] getSeed() {
        return callService(() -> service.getSeed(accessId));
    }

    private interface Function<R> {
        R apply();
    }

    private <R> R callService(SmartContract.Function<R> method) {

        Callable<R> callable = () -> {
            try {
                return method.apply();
            } catch (CreditsException e) {
                throw new RuntimeException(e);
            }
        };
        Future<R> future = cachedPool.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
