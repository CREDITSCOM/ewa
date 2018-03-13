public class InitialContractExecutionTestCode extends SmartContract {

    private int testInt;

    public InitialContractExecutionTestCode() throws Exception {
        System.out.println("The constructor has been invoked");
        System.out.println("testInt = " + testInt);
        testInt++;
    }

    public void foo() throws Exception {
        System.out.println("Invoking public void foo()");
        System.out.println("testInt = " + testInt);
        testInt += 10;
    }
}