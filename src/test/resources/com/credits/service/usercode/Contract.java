import java.net.ServerSocket;

public class Contract extends SmartContract {
    public void initialize() {
    }

    public void balanceGet() throws Exception {
        System.out.println("getBalance()");
        java.math.BigDecimal balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }

    public void openSocket() throws Exception {
        new ServerSocket(5555);
        System.out.println();
    }
};