public class ContractExecutorServiceTestCode extends SmartContract {

    private int total;

    public ContractExecutorServiceTestCode() throws Exception {
        String name = Thread.currentThread().getName();
        System.out.println("The constructor has been invoked. Thread: " + name);
        total = 1;
        System.out.println("Balance is " + getBalance("accXpfvxnZa8txuxpjyPqzBaqYPHqYu2rwn34lL8rjI=", "cs") + "Thread: " + name);
    }

    public void foo() throws Exception {
        System.out.println("Invoking public void foo()");
        total += 10;
    }
}