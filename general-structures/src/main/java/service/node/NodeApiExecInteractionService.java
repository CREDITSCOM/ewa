package service.node;


import pojo.apiexec.GetSmartCodeResultData;
import pojo.apiexec.SmartContractGetResultData;

import java.math.BigDecimal;

public interface NodeApiExecInteractionService {

    byte[] getSeed(long accessId);

    GetSmartCodeResultData getSmartCode(long accessId, String addressBase58);

    void sendTransaction(long accessId, String source, String target, double amount, double fee, byte[] userData);

    int getWalletId(long accessId, String addressBase58);

    SmartContractGetResultData getExternalSmartContractByteCode(long accessId, String addressBase58);

    BigDecimal getBalance(String addressBase58);
}
