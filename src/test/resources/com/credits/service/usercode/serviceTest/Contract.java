public class Contract extends SmartContract {

    private int total;

    @Override
    public void initialize() {
        String name = Thread.currentThread().getName();
        System.out.println("The initialize method has been invoked");
        total = 1;
    }

    public Contract() throws Exception {
    }

    public void addTokens(int amount) throws Exception {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public void printTotal() {
        System.out.println("total = " + total);
    }
}