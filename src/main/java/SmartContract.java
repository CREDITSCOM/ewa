import com.credits.Const;
import com.credits.common.utils.Converter;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.exception.ContractExecutorException;
import com.credits.leveldb.client.PoolData;
import com.credits.leveldb.client.TransactionData;
import com.credits.serialise.Serializer;
import com.credits.service.db.leveldb.LevelDbInteractionService;

import java.io.File;
import java.io.Serializable;
import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;

public abstract class SmartContract implements Serializable {

    protected static LevelDbInteractionService service;

    protected double total = 0;

    private String specialProperty;

    protected SmartContract() {
        File propertySerFile = Serializer.getPropertySerFile();
        String property;
        try {
            property = (String) Serializer.deserialize(propertySerFile, ClassLoader.getSystemClassLoader());
        } catch (ContractExecutorException e) {
            throw new RuntimeException(e);
        }
        this.specialProperty = property;
        propertySerFile.delete();
    }

    protected Double getBalance(String address, String currency) {
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

    protected void sendTransaction(String source, String target, Double amount, String currency) {
        try {
            Double csBalance = service.getBalance(source, Const.SYS_TRAN_CURRENCY);
            if (currency.equals(Const.SYS_TRAN_CURRENCY)) {
                double requestedAmount = amount + Const.FEE_TRAN_AMOUNT;
                if (csBalance < requestedAmount) {
                    throw new ContractExecutorException("Wallet's balance in credits is not enough for executing transaction.");
                }
            } else {
                Double currencyBalance = service.getBalance(source, currency);
                if (currencyBalance < amount) {
                    throw new ContractExecutorException("Wallet's balance in tokens is not enough for executing transaction.");
                }
                if (csBalance < Const.FEE_TRAN_AMOUNT) {
                    throw new ContractExecutorException("Wallet's balance in credits is not enough for executing transaction's fee.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        sendTransactionInternal(source, target, amount, currency);
        sendTransactionInternal(source, Const.SYS_TRAN_PUBLIC_KEY_BASE64, Const.FEE_TRAN_AMOUNT, Const.SYS_TRAN_CURRENCY);
    }

    private void sendTransactionInternal(String source, String target, Double amount, String currency) {
        String innerId = UUID.randomUUID().toString();

        try {
            byte[] hashBytes = Blake2S.generateHash(4);
            String hash = com.credits.leveldb.client.util.Converter.bytesToHex(hashBytes);
            byte[] privateKeyByteArr = Converter.decodeFromBASE64(this.specialProperty);
            PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
            String signatureBASE64 =
                Ed25519.generateSignOfTransaction(hash, innerId, source, target, amount, currency, privateKey);
            service.transactionFlow(hash, innerId, source, target, amount, currency, signatureBASE64);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
