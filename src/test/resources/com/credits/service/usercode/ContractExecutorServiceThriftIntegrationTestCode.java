public class ContractExecutorServiceThriftIntegrationTestCode extends SmartContract {

    public void foo() throws Exception {
        System.out.println("getBalance()");
        Double balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}