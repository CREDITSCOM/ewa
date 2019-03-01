public class AnnotationTest extends SmartContract {

    public int total;

    @Getter
    public void initialize() {
        String name = Thread.currentThread().getName();
        System.out.println("The initialize method has been invoked");
        total = 1;
    }

    public AnnotationTest() {
        System.out.println("It is initiator adderss - " + initiator);
    }

    @Getter
    @Contract(address = "test2", method = "notGetBalance")
    public void addTokens(int amount) {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public int getTotal() {
        System.out.println("total = " + total);
        return this.total;
    }


    @Contract(address = "test1", method = "getBalance")
    public void addToken(@Getter int amount) {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public void testToken(@ContractAddress(id = 0, address = "qwe") int amount) {

    }

    public void testNotToken(@ContractMethod(id = 0, method = "") int amount) {

    }
}