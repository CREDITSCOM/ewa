import com.credits.scapi.v0.SmartContract;


public class MySmartContract extends SmartContract {
    Integer value = new Integer(0);

    public MySmartContract(){
        System.out.println("Constructor");
    }

    public void initialize() {
    }

    public String balanceGet() throws Exception {
        System.out.println("getBalance()");
        java.math.BigDecimal balance = getBalance("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2");
        System.out.println("getBalance=" + balance);
        return balance.toString();
    }

    public void sendZeroCS() throws Exception {
        System.out.println("try to send 0 credits...");
        sendTransaction("1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2",  0, 1.0, new byte[0]);
        System.out.println("success");
    }

    public void useReflection() throws Exception {
        getClass().getConstructor().newInstance();
    }

    public Integer addValue(Integer value){
        return this.value += value;
    }
}