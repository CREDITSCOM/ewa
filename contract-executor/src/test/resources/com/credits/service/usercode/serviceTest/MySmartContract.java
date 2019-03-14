import java.util.Arrays;

public class MySmartContract extends SmartContract {

    public int total;

    public void initialize() {
        String name = Thread.currentThread().getName();
        System.out.println("The initialize method has been invoked");
        total = 1;
    }

    public MySmartContract() {
        System.out.println("It is initiator adderss - " + initiator);
    }

    public void addTokens(Integer amount) {
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

	
    public int externalCall(@ContractAddress(id = 0) String address, @ContractMethod(id = 0) String method) {
        return (int) invokeExternalContract(address, method, null);
    }

	@Contract(address = "FTWo7QNzweb7JMNL1kuFC32pdkTeQ716mhKThbzXQ9wK", method = "addTokens")
    public void externalCallChangeState(@ContractAddress(id = 0) String address, @ContractMethod(id = 0) String method, Integer value) {
        invokeExternalContract(address, method, value);
    }

    @Override
    public int hashCode() {
        return super.hashCode()+total;
    }

    public String getInitiatorAddress(){
        return initiator;
    }
}