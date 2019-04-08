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
    @ContractUsing(address = "test2", method = "notGetBalance")
    public void addTokens(int amount) {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public int getTotal() {
        System.out.println("total = " + total);
        return this.total;
    }


    @ContractUsing(address = "test1", method = "getBalance")
    public void addToken(@Getter int amount) {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public void testToken(@ContractAddress(id = 0) int amount) {

    }

    public void testNotToken(@ContractMethod(id = 0) int amount) {

    }

    @ContractUsing(address = "test1", method = "notGet")
    @ContractUsing(address = "test2", method = "notGetBalance")
    public void testMultiple1(int amount) {

    }

    @ContractsUsing(value={
        @ContractUsing(address = "test3", method = "notGetA"),
        @ContractUsing(address = "test2", method = "notGetBalance")
    })
    public void testMultiple2(int amount) {

    }
}