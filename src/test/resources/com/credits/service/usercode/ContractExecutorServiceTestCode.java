public class ContractExecutorServiceTestCode extends SmartContract {

    private int total;

    public ContractExecutorServiceTestCode() throws Exception {
        System.out.println("The constructor has been invoked");
        total = 1;
    }

    public void foo() throws Exception {
        System.out.println("Invoking public void foo()");
        total += 10;
    }
}