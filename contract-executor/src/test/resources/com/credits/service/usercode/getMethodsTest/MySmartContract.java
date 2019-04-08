public class MySmartContract extends SmartContract {

    public int total;

    public MySmartContract() {
        System.out.println("It is initiator adderss - " + initiator);
    }

    public void initialize() {
        String name = Thread.currentThread().getName();
        System.out.println("The initialize method has been invoked");
        total = 1;
    }

    public void addTokens(Integer amount) {
        total += amount;
        System.out.println(java.lang.Integer.toString(amount) + " tokens were added to total");
    }

    public boolean payable(String amount, String currency) throws Exception{
       return true;
   }

    @UsingContract(address = "address", method = "method")
    @UsingContract(address = "address", method = "method")
    public int externalCall(@ContractAddress(id = 0) String address, @ContractMethod(id = 0) String method) {
        return 0;
    }
}

