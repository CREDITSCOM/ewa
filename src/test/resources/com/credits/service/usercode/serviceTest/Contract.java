public class Contract extends SmartContract {

    private int total;

    public void initialize() {
        String name = Thread.currentThread().getName();
        System.out.println("The initialize method has been invoked");
        total = 1;
    }

    public Contract() {
    }

    public void addTokens(int amount) {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public void printTotal() {
        System.out.println("total = " + total);
    }

    public int getTotal() {
        System.out.println("total = " + total);
        return this.total;
    }

    public String getInitiatorAddress(){
        return initiator;
    }
}