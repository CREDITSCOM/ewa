import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.general.exception.CreditsException;
import com.credits.service.node.api.NodeApiInteractionService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;

    protected static NodeApiInteractionService service;
    protected static ExecutorService cachedPool;
    public transient String initiator = "";
    private transient String specialProperty = "";


    protected SmartContract() {
    }

    final protected BigDecimal getBalance(String address) {

        Callable<BigDecimal> callable = new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() {
                byte currencyByte = (byte) 1;
                try {
                    return service.getBalance(address, currencyByte);
                } catch (CreditsException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Future<BigDecimal> future = cachedPool.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    final protected TransactionData getTransaction(String transactionId) {
        try {
            return service.getTransaction(transactionId);
        } catch (CreditsException e) {
            throw new RuntimeException(e);
        }
    }

    final protected List<TransactionData> getTransactions(String address, long offset, long limit) {
        try {
            return service.getTransactions(address, offset, limit);
        } catch (CreditsException e) {
            throw new RuntimeException(e);
        }
    }

    final protected List<PoolData> getPoolList(long offset, long limit) {
        try {
            return service.getPoolList(offset, limit);
        } catch (CreditsException e) {
            throw new RuntimeException(e);
        }
    }

    final protected PoolData getPoolInfo(byte[] hash, long index) {
        try {
            return service.getPoolInfo(hash, index);
        } catch (CreditsException e) {
            throw new RuntimeException(e);
        }
    }

    final protected void sendTransaction(String target, double amount, double fee, byte[] userData) {
        try {
            service.transactionFlow(initiator, target, amount, fee, userData, specialProperty);
        } catch (CreditsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
