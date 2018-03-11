import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import com.credits.service.db.leveldb.LevelDbInteractionService;

import java.io.Serializable;
import java.util.List;

public class SmartContract implements Serializable {

    protected static LevelDbInteractionService service;


    protected Double getBalance(String address, String currency) throws Exception {
        return service.getBalance(address, currency);
    }

    protected TransactionData getTransaction(String transactionId) throws Exception {
        return service.getTransaction(transactionId);
    }

    protected List<TransactionData> getTransactions(String address, long offset, long limit) throws Exception {
        return service.getTransactions(address, offset, limit);
    }

    protected List<PoolData> getPoolList(long offset, long limit) throws Exception {
        return service.getPoolList(offset, limit);
    }

    protected PoolData getPool(String poolNumber) throws Exception {
        return service.getPool(poolNumber);
    }

    protected void sendTransaction(String hash, String innerId, String source, String target, Double amount, String currency) throws Exception {
        service.transactionFlow(hash, innerId, source, target, amount, currency);
    }

    protected String generateHash() {
        return service.getHash();
    }

    protected String generateInnerId() {
        return service.getInnerId();
    }
}
