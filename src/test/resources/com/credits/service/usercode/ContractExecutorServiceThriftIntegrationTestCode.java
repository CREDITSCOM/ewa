public class ContractExecutorServiceThriftIntegrationTestCode extends SmartContract {

    public void initialize(){};

    public void foo() throws Exception {
        System.out.println("getBalance()");
        java.math.BigDecimal balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}