import java.util.HashMap;
import java.util.Map;

public class Contract extends SmartContract {

    private Map<String, Integer> actions;
    private Map<String, Integer> balances;
    private String publicKey;

    public Contract() {
        publicKey = "userPublicKey";
        actions = new HashMap<>();
        actions.put(publicKey, 1);
        actions.put("testKey1", 2);
        actions.put("testKey2", 3);
        actions.put("testKey3", 4);
        balances = new HashMap<>();
        balances.put(publicKey, 100);
        balances.put("testKey1", 10);
        balances.put("testKey2", 20);
        balances.put("testKey3", 30);
    }

    public void changeAction(int id) {
        if (id == 1 || id == 2 || id == 3 || id == 4) {
            actions.put(publicKey, id);
        }
    }

    public void burnLine() {
        balances.computeIfPresent(publicKey, (k, v) -> v + 10);
    }

    public Map<String, Integer> getBalance(int id) {
        Map<String, Integer> resultBalances = new HashMap<>();
        if (id == 1) {
            resultBalances.put(publicKey, balances.get(publicKey));
        }
        if (id == 0) {
            balances.forEach(resultBalances::put);
        }
        return resultBalances;
    }

    public Integer getCurrentAction() {
        return actions.get(publicKey);
    }
}