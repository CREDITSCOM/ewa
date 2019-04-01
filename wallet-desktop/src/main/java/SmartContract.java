import java.io.Serializable;

public abstract class SmartContract implements Serializable {

    private static final long serialVersionUID = -7544650022718657167L;
    protected final transient long accessId;
    protected final transient String initiator;
    protected final String contractAddress;

    public SmartContract() {
        initiator = null;
        accessId = 0;
        contractAddress = null;
    }

    final protected void sendTransaction(String from, String to, double amount, double fee, byte... userData) {
    }

    final protected Object invokeExternalContract(String contractAddress, String method, Object... params) {
        return null;
    }


    final protected byte[] getSeed() {
        return null;
    }
}
