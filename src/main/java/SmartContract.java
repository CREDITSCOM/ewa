import com.credits.Const;
import com.credits.common.utils.Converter;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.leveldb.client.util.LevelDbClientConverter;
import com.credits.serialise.Serializer;
import com.credits.service.db.leveldb.LevelDbInteractionService;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;

public abstract class SmartContract implements Serializable {

    protected static LevelDbInteractionService service;

    protected double total = 0;

    private String specialProperty;

    protected SmartContract() {
        Class<?> clazz = this.getClass();
        String fileName = clazz.getSimpleName() + ".class";
        URL fileURL = clazz.getClassLoader().getResource(fileName);
        String loadAddress = new File(fileURL.getFile()).getParentFile().getName();

        File propertySerFile = Serializer.getPropertySerFile(loadAddress);
        String property;
        try {
            property = (String) Serializer.deserialize(propertySerFile, ClassLoader.getSystemClassLoader());
        } catch (ContractExecutorException e) {
            throw new RuntimeException(e);
        }
        this.specialProperty = property;
        propertySerFile.delete();
    }

    protected BigDecimal getBalance(String address, String currency) {
        try {
            return service.getBalance(address, currency);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected TransactionData getTransaction(String transactionId) {
        try {
            return service.getTransaction(transactionId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected List<TransactionData> getTransactions(String address, long offset, long limit) {
        try {
            return service.getTransactions(address, offset, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected List<PoolData> getPoolList(long offset, long limit) {
        try {
            return service.getPoolList(offset, limit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected PoolData getPool(String poolNumber) {
        try {
            return service.getPool(poolNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendTransaction(String source, String target, double amount, String currency) {
        try {
            BigDecimal decAmount = new BigDecimal(String.valueOf(amount));
            TransactionFlowData transactionData = makeTransactionFlowData(source, target, decAmount, currency);
            TransactionFlowData feeTransactionData = makeTransactionFlowData(source, Const.SYS_TRAN_PUBLIC_KEY, Const.FEE_TRAN_AMOUNT, Const.SYS_TRAN_CURRENCY);

            service.transactionFlowWithFee(transactionData, feeTransactionData, true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private TransactionFlowData makeTransactionFlowData(String source, String target, BigDecimal amount, String currency) {
        String innerId = UUID.randomUUID().toString();

        try {
            byte[] hashBytes = Blake2S.generateHash(4);
            String hash = LevelDbClientConverter.bytesToHex(hashBytes);
            byte[] privateKeyByteArr = Converter.decodeFromBASE58(this.specialProperty);
            PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            String signatureBASE58 =
                Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency, privateKey);
            return new TransactionFlowData(hash, innerId, source, target, amount, currency, signatureBASE58);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
