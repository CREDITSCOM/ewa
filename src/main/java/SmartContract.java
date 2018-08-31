import com.credits.common.exception.CreditsCommonException;
import com.credits.leveldb.client.data.PoolData;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.service.db.leveldb.LevelDbInteractionService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

public abstract class SmartContract implements Serializable {

    protected static LevelDbInteractionService service;

    protected double total = 0;

    public transient String initiator;

    private String specialProperty;

    protected SmartContract() {
    }

    final protected BigDecimal getBalance(String address, String currency) {
        byte currencyByte = (byte) 1;
        try {
            return service.getBalance(address, currencyByte);
        } catch (LevelDbClientException | CreditsNodeException | CreditsCommonException e) {
            throw new RuntimeException(e);
        }
    }

    final protected TransactionData getTransaction(String transactionId) {
        try {
            return service.getTransaction(transactionId);
        } catch (LevelDbClientException | CreditsNodeException e) {
            throw new RuntimeException(e);
        }
    }

    final protected List<TransactionData> getTransactions(String address, long offset, long limit) {
        try {
            return service.getTransactions(address, offset, limit);
        } catch (LevelDbClientException | CreditsNodeException | CreditsCommonException e) {
            throw new RuntimeException(e);
        }
    }

    final protected List<PoolData> getPoolList(long offset, long limit) {
        try {
            return service.getPoolList(offset, limit);
        } catch (LevelDbClientException | CreditsNodeException e) {
            throw new RuntimeException(e);
        }
    }

    final protected PoolData getPoolInfo(byte[] hash, long index) {
        try {
            return service.getPoolInfo(hash, index);
        } catch (LevelDbClientException | CreditsNodeException e) {
            throw new RuntimeException(e);
        }
    }

    final protected void sendTransaction(String source, String target, double amount, String currency, double fee) {
        try {
            byte currencyByte = (byte) 1;
            BigDecimal decAmount = new BigDecimal(String.valueOf(amount));
//            byte[] innerIdhashBytes = Blake2S.generateHash(4);
//            String innerId = Converter.bytesToHex(innerIdhashBytes);

//            byte[] privateKeyByteArr = Converter.decodeFromBASE58(this.specialProperty);
//            PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);

            BigDecimal balance = service.getBalance(source, currencyByte);

            BigDecimal decFee = new BigDecimal(String.valueOf(fee));

            byte[] signature = new byte[0];
            String signatureBASE58 = "";
//                Ed25519.generateSignOfTransaction(innerId, source, target, decAmount, balance, currency, privateKey);

            Instant instant = Instant.now();
            service.transactionFlow(instant.toEpochMilli(), source, target, decAmount, balance, currencyByte, signature, decFee);
        } catch (LevelDbClientException | CreditsNodeException | CreditsCommonException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
