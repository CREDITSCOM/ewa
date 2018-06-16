import com.credits.leveldb.client.data.PoolData;
import com.credits.leveldb.client.data.TransactionData;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public abstract class SmartContract implements Serializable {

    protected double total = 0;

    protected BigDecimal getBalance(String address, String currency) {
        return null;
    }

    protected TransactionData getTransaction(String transactionId) {
        return null;
    }

    protected List<TransactionData> getTransactions(String address, long offset, long limit) {
        return null;
    }

    protected List<PoolData> getPoolList(long offset, long limit) {
        return null;
    }

    protected PoolData getPoolInfo(byte[] hash, long index) {
        return null;
    }

    protected void sendTransaction(String source, String target, double amount, String currency) {
    }
}
