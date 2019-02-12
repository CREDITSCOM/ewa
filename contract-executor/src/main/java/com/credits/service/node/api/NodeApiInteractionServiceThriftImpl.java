package com.credits.service.node.api;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.util.TransactionIdCalculateUtils;
import com.credits.general.util.Base58;
import com.credits.general.util.exception.ConverterException;
import com.credits.ioc.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class NodeApiInteractionServiceThriftImpl implements NodeApiInteractionService {

    private final static Logger logger = LoggerFactory.getLogger(NodeApiInteractionServiceThriftImpl.class);

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
    public byte[] getSeed(long accessId) {
        return service.getSeed(accessId);
    }

    @Override
    public void transactionFlow(String source, String target, double amount, double fee, byte[] userData) throws ConverterException, NodeClientException {
        TransactionIdCalculateUtils.CalcTransactionIdSourceTargetResult calcTransactionIdSourceTargetResult =
            TransactionIdCalculateUtils.calcTransactionIdSourceTarget(service, source, target, false);
        BigDecimal decAmount = new BigDecimal(String.valueOf(amount));

        short maxFee = 0x6648; //TODO need add fee converter from BigDecimal to short

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(calcTransactionIdSourceTargetResult.getTransactionId(), Base58.decode(calcTransactionIdSourceTargetResult.getSource()), Base58.decode(calcTransactionIdSourceTargetResult.getTarget()), decAmount, maxFee,  null, userData);
        service.transactionFlow(transactionFlowData);
    }
}
