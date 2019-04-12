import com.credits.scapi.v1.SmartContract;

public class MySmartContractV1 extends SmartContract {

    public MySmartContractV1() {
        System.out.println();
    }

    @Override
    public BigDecimal getBalance(String addressBase58) {
        return super.getBalance(addressBase58);
    }
}