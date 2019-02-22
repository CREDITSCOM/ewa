package com.credits.service.node.apiexec;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.pojo.apiexec.GetSmartCodeResultData;

public interface NodeApiExecService {

    byte[] getSeed(long accessId) throws NodeClientException;

    GetSmartCodeResultData getSmartCode(long accessId, String addressBase58) throws NodeClientException;

    void sendTransaction(TransactionFlowData transactionFlowData) throws NodeClientException;

    int getWalletId(String addressBase58) throws NodeClientException;
}
