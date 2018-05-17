import java.io.Serializable;

public abstract class SmartContract implements Serializable {

    protected double total = 0;

    protected void getBalance(String address, String currency) {
    }

    protected void getTransaction(String transactionId) {
    }

    protected void getTransactions(String address, long offset, long limit) {
    }

    protected void getPoolList(long offset, long limit) {
    }

    protected void getPoolInfo(byte[] hash, long index) {
    }

    protected void sendTransaction(String source, String target, double amount, String currency) {
    }
}
