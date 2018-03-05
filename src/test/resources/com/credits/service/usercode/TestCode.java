public class TestCode extends SmartContract {

    public TestCode() throws Exception {
        System.out.println("The constructor has been invoked");

        Double balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2", "CS");
        System.out.println("getBalance=" + balance);
    }
}