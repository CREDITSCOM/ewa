import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.general.exception.CreditsException;
import com.credits.service.node.api.NodeApiInteractionService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;

    protected static NodeApiServiceImpl nodeApiService;
    protected static NodeApiInteractionService service;
    protected static TransactionIdCalculateUtils transactionIdCalculateUtils;

    public transient String initiator = "";
    private String specialProperty;

    protected SmartContract() {
    }

    final protected BigDecimal getBalance(String address) {
        byte currencyByte = (byte) 1;
        try {
            return service.getBalance(address, currencyByte);
        } catch (CreditsException e) {
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
            byte currencyByte = (byte) 1;
            BigDecimal decAmount = new BigDecimal(String.valueOf(amount));
            BigDecimal decFee = new BigDecimal(String.valueOf(fee));
            //todo add signature
            byte[] signature = new byte[0];
            Instant instant = Instant.now();
            TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult =
                TransactionIdCalculateUtils.calcTransactionIdSourceTarget(nodeApiService, initiator, target);
            service.transactionFlow(calcTransactionIdSourceTargetResult.getTransactionId(), initiator, target, decAmount, signature, decFee, userData);
        } catch (CreditsException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
