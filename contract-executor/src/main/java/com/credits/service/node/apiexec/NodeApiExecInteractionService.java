package com.credits.service.node.apiexec;


import com.credits.pojo.apiexec.GetSmartCodeResultData;

public interface NodeApiExecInteractionService {

    byte[] getSeed(long accessId);

    GetSmartCodeResultData getSmartCode(long accessId, String addressBase58);

    void sendTransaction(String source, String target, double amount, double fee, byte[] userData);

    int getWalletId(String addressBase58);
}
