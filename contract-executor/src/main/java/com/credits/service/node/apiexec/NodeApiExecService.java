package com.credits.service.node.apiexec;


import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.exception.ApiClientException;
import com.credits.pojo.apiexec.GetSmartCodeResultData;
import com.credits.pojo.apiexec.SmartContractGetResultData;

public interface NodeApiExecService {

    byte[] getSeed(long accessId) throws NodeClientException;

    GetSmartCodeResultData getSmartCode(long accessId, String addressBase58) throws NodeClientException;

    void sendTransaction(TransactionFlowData transactionFlowData) throws NodeClientException;

    SmartContractGetResultData getSmartContractBinary(long accessId, String addressBase58) throws ApiClientException;

    int getWalletId(String addressBase58) throws NodeClientException;
}
