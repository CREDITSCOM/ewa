import com.credits.common.utils.Converter;
import com.credits.common.utils.Utils;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import com.credits.service.db.leveldb.LevelDbInteractionService;

import java.io.Serializable;
import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;

public abstract class SmartContract implements Serializable {

    private static final String SYS_TRAN_PUBLIC_KEY_BASE64 = "accXpfvxnZa8txuxpjyPqzBaqYPHqYu2rwn34lL8rjI=";

    protected static LevelDbInteractionService service;

    protected double total = 0;

    private String specialProperty;

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

    protected void sendTransaction(String source, String target, Double amount, String currency) throws Exception {
        String hash = Utils.randomAlphaNumeric(8);
        String innerId = UUID.randomUUID().toString();

        byte[] privateKeyByteArr = Converter.decodeFromBASE64(this.specialProperty);
        PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        String signatureBASE64 =
            Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency, privateKey);

        service.transactionFlow(hash, innerId, source, target, amount, currency, signatureBASE64);
    }

    private void sendTransactionSystem(String target, Double amount, String currency) throws Exception {
        sendTransaction(SmartContract.SYS_TRAN_PUBLIC_KEY_BASE64, target, amount, currency);
    }
}
