import com.credits.scapi.v0.SmartContract;


import java.util.*;

public class MySmartContract extends SmartContract {

    private Map<String, Integer> actions;
    private Map<String, Integer> balances;
    private String publicKey;

    public MySmartContract() {
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

    public Set<String> getSetOfString() {
        Set<String> set = new HashSet<>();
        set.add("Hello");
        return set;
    }

    public Set<Integer> getSetOfInteger() {
        Set<Integer> set = new HashSet<>();
        set.add(555);
        return set;
    }

    public List<Double> getListOfDouble() {
        List<Double> list = new ArrayList<>();
        list.add(5.55);
        return list;
    }

    public List<String> getListOfString() {
        List<String> list = new ArrayList<>();
        list.add("Hello");
        return list;
    }

    public Double getDouble() {
        return 5.55;
    }

    public Byte getByte() {
        return (byte) 5;
    }

    public String getString() {
        return "Hello";
    }
}