import com.credits.scapi.annotations.ContractAddress;
import com.credits.scapi.annotations.ContractMethod;
import com.credits.scapi.annotations.UsingContract;
import java.lang.Integer;
import com.credits.scapi.v0.SmartContract;

public class MySmartContract extends SmartContract {

    private int total;

    public MySmartContract() {
        super();
    }

    public void addTokens(int amount) {
        total += amount;
        System.out.println(Integer.toString(amount) + " tokens were added to total");
    }

    public int getTokens(){
        return total;
    }
}

