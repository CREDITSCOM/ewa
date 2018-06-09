import com.credits.common.utils.Converter;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.data.PoolData;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.serialise.Serializer;
import com.credits.service.db.leveldb.LevelDbInteractionService;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.security.PrivateKey;
import java.util.List;

public abstract class SmartContract implements Serializable {

    protected static LevelDbInteractionService service;

    protected double total = 0;

    private String specialProperty;

    protected SmartContract() {
    }

    abstract protected void initialize();

    final protected BigDecimal getBalance(String address, String currency) {
        try {
            return service.getBalance(address, currency);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected TransactionData getTransaction(String transactionId) {
        try {
            return service.getTransaction(transactionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected List<TransactionData> getTransactions(String address, long offset, long limit) {
        try {
            return service.getTransactions(address, offset, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected List<PoolData> getPoolList(long offset, long limit) {
        try {
            return service.getPoolList(offset, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected PoolData getPoolInfo(byte[] hash, long index) {
        try {
            return service.getPoolInfo(hash, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final protected void sendTransaction(String source, String target, double amount, String currency) {
        try {
            BigDecimal decAmount = new BigDecimal(String.valueOf(amount));
//            byte[] innerIdhashBytes = Blake2S.generateHash(4);
//            String innerId = Converter.bytesToHex(innerIdhashBytes);

//            byte[] privateKeyByteArr = Converter.decodeFromBASE58(this.specialProperty);
//            PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);

            BigDecimal balance = service.getBalance(source, currency);

            String signatureBASE58 = "";
//                Ed25519.generateSignOfTransaction(innerId, source, target, decAmount, balance, currency, privateKey);

            service.transactionFlow("", source, target, decAmount, balance, currency, signatureBASE58);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
