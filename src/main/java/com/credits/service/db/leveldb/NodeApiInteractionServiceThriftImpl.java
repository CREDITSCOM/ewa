package com.credits.service.db.leveldb;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.CreateTransactionData;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.service.NodeApiService;
import com.credits.general.util.Base58;
import com.credits.general.util.exception.ConverterException;
import com.credits.ioc.Injector;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class NodeApiInteractionServiceThriftImpl implements NodeApiInteractionService {

    @Inject
    NodeApiService service;

    public NodeApiInteractionServiceThriftImpl() {
        Injector.INJECTOR.component.inject(this);
    }

    @Override
    public BigDecimal getBalance(String address, byte currency) throws NodeClientException, ConverterException {
        return service.getBalance(address);
    }

    @Override
    public TransactionData getTransaction(String transactionId) throws NodeClientException {
        //TODO need add adapter from String to TransactionIdData
        return service.getTransaction(new TransactionIdData());
    }

    @Override
    public List<TransactionData> getTransactions(String address, long offset, long limit) throws NodeClientException, ConverterException {
        return service.getTransactions(address, offset, limit);
    }

    @Override
    public List<PoolData> getPoolList(long offset, long limit) throws NodeClientException {
        return service.getPoolList(offset, limit);
    }

    @Override
    public PoolData getPoolInfo(byte[] hash, long index) throws NodeClientException {
        return service.getPoolInfo(hash, index);
    }

    @Override
    public void transactionFlow(Long innerId, String source, String target, BigDecimal amount, BigDecimal balance, byte currency, byte[] signature,
                                BigDecimal fee) throws ConverterException, NodeClientException {
        short maxFee = 0x6648; //TODO need add fee converter from BigDecimal to short
        CreateTransactionData CreateTransactionData =
            new CreateTransactionData(System.currentTimeMillis(), Base58.decode(source), Base58.decode(target), amount, balance, currency, maxFee,
                signature);
        service.createTransaction(CreateTransactionData, true);
    }
}
