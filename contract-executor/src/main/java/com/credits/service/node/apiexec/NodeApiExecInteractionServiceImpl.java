package com.credits.service.node.apiexec;

import com.credits.client.executor.thrift.generated.apiexec.GetSeedResult;
import com.credits.client.executor.thrift.generated.apiexec.SmartContractGetResult;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.thrift.generated.WalletBalanceGetResult;
import com.credits.client.node.thrift.generated.WalletIdGetResult;
import com.credits.general.thrift.generated.Amount;
import com.credits.ioc.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.apiexec.GetSmartCodeResultData;
import pojo.apiexec.SmartContractGetResultData;
import service.node.NodeApiExecInteractionService;

import javax.inject.Inject;
import java.math.BigDecimal;

import static com.credits.client.node.util.NodeClientUtils.processApiResponse;
import static com.credits.client.node.util.NodePojoConverter.transactionFlowDataToTransaction;
import static com.credits.general.util.GeneralConverter.amountToBigDecimal;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.Utils.calculateActualFee;
import static com.credits.utils.ApiExecClientPojoConverter.createSmartContractGetResultData;

public class NodeApiExecInteractionServiceImpl implements NodeApiExecInteractionService {
    private final static Logger logger = LoggerFactory.getLogger(NodeApiExecInteractionServiceImpl.class);

    private final NodeThriftApiExec nodeClient;

    @Inject
    public NodeApiExecInteractionServiceImpl(NodeThriftApiExec nodeApiClient) {
        nodeClient = nodeApiClient;
        Injector.INJECTOR.component.inject(this);
    }

    @Override
    public byte[] getSeed(long accessId) {
        logger.debug(String.format("getSeed: ---> accessId = %s", accessId));
        GetSeedResult seed = nodeClient.getSeed(accessId);
        processApiResponse(seed.getStatus());
        logger.debug(String.format("getSeed: <--- seed = %s", seed.getSeed()));
        return seed.getSeed();
    }

    @Override
    @Deprecated
    public GetSmartCodeResultData getSmartCode(long accessId, String addressBase58) {
        return null;
    }

    @Override
    public SmartContractGetResultData getExternalSmartContractByteCode(long accessId, String addressBase58) {
        logger.debug(String.format("getExternalSmartContractByteCode: ---> accessId = %s; addressBase58 = %s", accessId, addressBase58));
        SmartContractGetResult result = nodeClient.getSmartContractBinary(accessId, decodeFromBASE58(addressBase58));
        processApiResponse(result.getStatus());
        SmartContractGetResultData data = createSmartContractGetResultData(result);
        logger.debug(String.format("getExternalSmartContractByteCode: <--- result = %s", data));
        return data;
    }

    @Override
    public void sendTransaction(long accessId, String source, String target, double amount, double fee, byte[] userData) {
        final var decAmount = new BigDecimal(String.valueOf(amount));
        final var actualOfferedMaxFee = calculateActualFee(fee);
        final var transactionFlowData = new TransactionFlowData(0,
                                                          decodeFromBASE58(source),
                                                          decodeFromBASE58(target),
                                                          decAmount,
                                                          actualOfferedMaxFee.getRight(),
                                                          null,
                                                          userData);
        logger.debug("sendTransaction transactionFlowData -> {}", transactionFlowData);
        final var result = nodeClient.sendTransaction(accessId, transactionFlowDataToTransaction(transactionFlowData));
        processApiResponse(result.getStatus());
    }

    @Override
    public int getWalletId(long accessId, String addressBase58) {
        logger.debug(String.format("getWalletId: ---> addressBase58 = %s", addressBase58));
        WalletIdGetResult result = nodeClient.getWalletId(accessId, decodeFromBASE58(addressBase58));
        processApiResponse(result.getStatus());
        logger.debug(String.format("getWalletId: <--- walletId = %s", result.getWalletId()));
        return result.getWalletId();
    }

    @Override
    public BigDecimal getBalance(String addressBase58) {
        logger.info(String.format("getBalance: ---> address = %s", addressBase58));
        WalletBalanceGetResult result = nodeClient.getBalance(decodeFromBASE58(addressBase58));
        processApiResponse(result.getStatus());
        Amount amount = result.getBalance();
        BigDecimal balance = amountToBigDecimal(amount);
        logger.info(String.format("getBalance: <--- balance = %s", balance));
        return balance;
    }
}
