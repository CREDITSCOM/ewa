import java.util.Arrays;
import java.math.BigDecimal;
import java.text.NumberFormat;

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

    public boolean payable(String amount, String currency) throws Exception{
	   NumberFormat nf = NumberFormat.getInstance(java.util.Locale.ENGLISH);
       BigDecimal val = new BigDecimal(nf.parse(amount).doubleValue());
       sendTransaction(initiator, contractAddress, val.doubleValue(), 1.0);
       return true;
   }
	
    public int externalCall(@ContractAddress(id = 0) String address, @ContractMethod(id = 0) String method) {
        return (int) invokeExternalContract(address, method);
    }

	@Contract(address = "FTWo7QNzweb7JMNL1kuFC32pdkTeQ716mhKThbzXQ9wK", method = "addTokens")
    public void externalCallChangeState(@ContractAddress(id = 0) String address, @ContractMethod(id = 0) String method, Integer value) {
        invokeExternalContract(address, method, value);
    }

    public Integer recursionExternalContractSetterCall(int count) {
        System.out.println("count = " + count);
        if (count-- > 0) {
            addTokens(count);
            return (int) invokeExternalContract(contractAddress, "recursionExternalContractSetterCall", count);
        }
        return getTotal();
    }

    @Override
    public int hashCode() {
        return super.hashCode()+total;
    }

    public String getInitiatorAddress(){
        return initiator;
    }
}