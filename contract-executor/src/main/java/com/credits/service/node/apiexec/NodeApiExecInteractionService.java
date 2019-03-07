package com.credits.service.node.apiexec;


import com.credits.pojo.apiexec.GetSmartCodeResultData;
import com.credits.pojo.apiexec.SmartContractGetResultData;

public interface NodeApiExecInteractionService {

    byte[] getSeed(long accessId);

    GetSmartCodeResultData getSmartCode(long accessId, String addressBase58);

    void sendTransaction(long accessId, String source, String target, double amount, double fee, byte[] userData);

    int getWalletId(long accessId, String addressBase58);

    SmartContractGetResultData getExternalSmartContractByteCode(long accessId, String addressBase58);
}
