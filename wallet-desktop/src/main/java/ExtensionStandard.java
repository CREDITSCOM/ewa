/**
 * Created by Igor Goryunov on 26.09.2018
 */
public interface ExtensionStandard extends BasicStandard {

    void register();

    boolean buyTokens(String amount);
}
