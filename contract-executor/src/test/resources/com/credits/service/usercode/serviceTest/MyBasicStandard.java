import com.credits.scapi.v0.BasicStandard;

public class MyBasicStandard implements BasicStandard {

    public MyBasicStandard() {
        System.out.println();
    }

    public String getName() {
        return "CS Credits";
    }

    public String getSymbol() {
        return "CS";
    }

    public int getDecimal() {return 0;}

    public boolean setFrozen(boolean frozen) {return false;}

    public String totalSupply() {return null;}

    public String balanceOf(String owner) {return null;}

    public String allowance(String owner, String spender) {return null;}

    public boolean transfer(String to, String amount) {return false;}

    public boolean transferFrom(String from, String to, String amount) {return false;}

    public void approve(String spender, String amount) {}

    public boolean burn(String amount) {return false;}

}