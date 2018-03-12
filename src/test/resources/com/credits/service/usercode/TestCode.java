public class TestCode extends SmartContract {

    private int testInt;

    public TestCode() throws Exception {
        System.out.println("The constructor has been invoked");
        System.out.println("testInt = " + testInt);
        testInt++;

        Double balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }

    public void foo() throws Exception {
        System.out.println("Invoking public void foo()");
        System.out.println("testInt = " + testInt);
        testInt += 10;

        Double balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}