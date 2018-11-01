import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.TransactionData;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;

    protected transient String initiator;

    final protected BigDecimal getBalance(String address, String currency) {
        return null;
    }

    final protected TransactionData getTransaction(String transactionId) {
        return null;
    }

    final protected List<TransactionData> getTransactions(String address, long offset, long limit) {
        return null;
    }

    final protected List<PoolData> getPoolList(long offset, long limit) {
        return null;
    }

    final protected PoolData getPoolInfo(byte[] hash, long index) {
        return null;
    }

    final protected void sendTransaction(String source, String target, double amount, String currency, double fee) {
    }
}