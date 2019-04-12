package com.credits.service.node.apiexec;

import com.credits.client.executor.thrift.generated.apiexec.GetSeedResult;
import com.credits.client.executor.thrift.generated.apiexec.SendTransactionResult;
import com.credits.client.executor.thrift.generated.apiexec.SmartContractGetResult;
import com.credits.client.node.thrift.generated.Transaction;
import com.credits.client.node.thrift.generated.WalletBalanceGetResult;
import com.credits.client.node.thrift.generated.WalletIdGetResult;
import com.credits.exception.ApiClientException;

interface NodeThriftApiExec {
    GetSeedResult getSeed(long accessId) throws ApiClientException;

    SmartContractGetResult getSmartContractBinary(long accessId, byte[] address) throws ApiClientException;

    SendTransactionResult sendTransaction(long accessId, Transaction transaction) throws ApiClientException;

    WalletIdGetResult getWalletId(long accessId, byte[] address) throws ApiClientException;

    WalletBalanceGetResult getBalance(byte[] address) throws ApiClientException;
}
