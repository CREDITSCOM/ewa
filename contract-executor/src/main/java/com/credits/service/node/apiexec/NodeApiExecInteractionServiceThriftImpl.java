package com.credits.service.node.apiexec;

import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.Utils;
import com.credits.ioc.Injector;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.apiexec.GetSmartCodeResultData;
import pojo.apiexec.SmartContractGetResultData;
import service.node.NodeApiExecInteractionService;

import javax.inject.Inject;
import java.math.BigDecimal;

public class NodeApiExecInteractionServiceThriftImpl implements NodeApiExecInteractionService {

    private final static Logger logger = LoggerFactory.getLogger(NodeApiExecInteractionServiceThriftImpl.class);

    @Inject
    NodeApiExecService service;

    public NodeApiExecInteractionServiceThriftImpl() {
        Injector.INJECTOR.component.inject(this);
    }

    @Override
    public byte[] getSeed(long accessId) {
        return service.getSeed(accessId);
    }



    @Override
    public GetSmartCodeResultData getSmartCode(long accessId, String addressBase58) {
        return service.getSmartCode(accessId, addressBase58);
    }

    @Override
    public SmartContractGetResultData getExternalSmartContractByteCode(long accessId, String addressBase58) {
        return service.getSmartContractBinary(accessId,addressBase58);
    }

    @Override
    public void sendTransaction(long accessId, String source, String target, double amount, double fee, byte[] userData) {
        BigDecimal decAmount = new BigDecimal(String.valueOf(amount));

        Pair<Double, Short> actualOfferedMaxFee = Utils.calculateActualFee(fee);

        TransactionFlowData transactionFlowData =
                new TransactionFlowData(0, GeneralConverter.decodeFromBASE58(source), GeneralConverter.decodeFromBASE58(target), decAmount, actualOfferedMaxFee.getRight(),  null, userData);
        service.sendTransaction(accessId, transactionFlowData);
    }

    @Override
    public int getWalletId(long accessId, String addressBase58) {
        return service.getWalletId(accessId, addressBase58);
    }

    @Override
    public BigDecimal getBalance(String addressBase58) {
        return service.getBalance(addressBase58);
    }
}
