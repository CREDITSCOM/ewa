package com.credits.client.node.util;

import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.generated.Amount;
import com.credits.client.node.thrift.generated.AmountCommission;
import com.credits.client.node.thrift.generated.Pool;
import com.credits.client.node.thrift.generated.SealedTransaction;
import com.credits.client.node.thrift.generated.SmartContract;
import com.credits.client.node.thrift.generated.SmartContractDeploy;
import com.credits.client.node.thrift.generated.SmartContractInvocation;
import com.credits.client.node.thrift.generated.Transaction;
import com.credits.client.node.thrift.generated.TransactionFlowResult;
import com.credits.client.node.thrift.generated.TransactionId;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.exception.ConverterException;
import com.credits.general.util.variant.VariantConverter;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.credits.general.util.Constants.ds;
import static com.credits.general.util.GeneralConverter.byteArrayToByteBuffer;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class NodePojoConverter {

    public static Double amountToDouble(Amount amount) {

        int integralPart = amount.getIntegral();
        double fractionPart = amount.getFraction() / 1000000000000000000D;
        return (double) integralPart + fractionPart;
    }

    public static Amount doubleToAmount(Double value) throws ConverterException {
        if (value == null) {
            throw new ConverterException("value is null");
        }
        String valueAsString = String.format(Locale.ROOT, "%.18f", value);
        String[] valueDelimited = valueAsString.split("\\.");
        int integral = Integer.parseInt(valueDelimited[0]);
        long fraction = Long.parseLong(valueDelimited[1]);
        return new Amount(integral, fraction);
    }

    @SuppressWarnings("unsupported")
    public static Amount bigDecimalToAmount(BigDecimal value) throws ConverterException {

        if (value == null) {
            throw new ConverterException("value is null");
        }

        int integral;

        long fraction;

        String valueAsString = GeneralConverter.toString(value);

        if (valueAsString.contains(ds)) {
            String[] valueDelimited = valueAsString.split("[" + ds + "]");
            integral = Integer.parseInt(valueDelimited[0]);
            String fractionAsString = String.format("%-18s", valueDelimited[1]).replace(' ', '0');
            fraction = Long.parseLong(fractionAsString);
        } else {
            integral = Integer.parseInt(valueAsString);
            fraction = 0L;
        }
        return new Amount(integral, fraction);
    }

    public static TransactionData transactionToTransactionData(SealedTransaction sealedTransaction) {

        TransactionId blockTransactionId = sealedTransaction.getId();
        Transaction transaction = sealedTransaction.getTrxn();

        TransactionData data = new TransactionData();
        Long innerId = transaction.getId();
        data.setBlockId(GeneralConverter.encodeToBASE58(blockTransactionId.getPoolHash()) +"."+ blockTransactionId.getIndex());
        data.setAmount(NodePojoConverter.amountToBigDecimal(transaction.getAmount()));
        data.setCurrency(transaction.getCurrency());
        data.setId(innerId);
        data.setSource(transaction.getSource());
        data.setTarget(transaction.getTarget());
        data.setBalance(NodePojoConverter.amountToBigDecimal(transaction.getBalance()));
        data.setCommentBytes(transaction.getUserFields());
        if (transaction.getSmartContract() != null) {
            data.setMethod(transaction.getSmartContract().getMethod());
            data.setParams(variantListToVariantDataList(transaction.getSmartContract().getParams()));
        }
        return data;
    }

    private static List<VariantData> variantListToVariantDataList(List<Variant> params) {
        ArrayList<VariantData> objectParams = new ArrayList<>();
        params.forEach(object -> objectParams.add(VariantConverter.variantToVariantData(object)));
        return objectParams;
    }

    public static TransactionData transactionToTransactionData(Transaction transaction) {
        TransactionData data = new TransactionData();
        Long innerId = transaction.getId();
        if (transaction.getAmount() == null) {
            data.setAmount(BigDecimal.ZERO);
        } else {
            data.setAmount(NodePojoConverter.amountToBigDecimal(transaction.getAmount()));
        }
        data.setCurrency(transaction.getCurrency());
        data.setId(innerId);
        data.setSource(transaction.getSource());
        data.setTarget(transaction.getTarget());
        if (transaction.getBalance() == null) {
            data.setBalance(BigDecimal.ZERO);
        } else {
            data.setBalance(NodePojoConverter.amountToBigDecimal(transaction.getBalance()));
        }
        data.setCommentBytes(transaction.getUserFields());
        return data;
    }

    public static BigDecimal amountToBigDecimal(Amount amount) {

        int integralPart = amount.getIntegral();
        long fractionPart = amount.getFraction();

        String integralPartAsString = GeneralConverter.toString(integralPart);
        String fractionPartAsString = GeneralConverter.toString(fractionPart);

        return new BigDecimal(integralPartAsString + "." + fractionPartAsString);
    }

    public static WalletData walletToWalletData(com.credits.client.node.thrift.generated.WalletData walletData) {

        return new WalletData(walletData.getWalletId(), NodePojoConverter.amountToBigDecimal(walletData.getBalance()),
            walletData.getLastTransactionId());
    }

    public static PoolData poolToPoolData(Pool pool) {

        PoolData data = new PoolData();

        data.setTransactionsCount(pool.getTransactionsCount());
        data.setHash(pool.getHash());
        data.setPrevHash(pool.getPrevHash());
        data.setTime(pool.getTime());
        data.setPoolNumber(pool.getPoolNumber());
        return data;
    }

    public static SmartContract smartContractDataToSmartContract(SmartContractData smartContractData) {

        SmartContract smartContract = new SmartContract();
        smartContract.setAddress(smartContractData.getAddress());
        smartContract.setDeployer(smartContractData.getDeployer());
        smartContract.setSmartContractDeploy(NodePojoConverter.smartContractDeployDataToSmartContractDeploy(
            smartContractData.getSmartContractDeployData()));
        return smartContract;
    }

    public static SmartContractDeploy smartContractDeployDataToSmartContractDeploy(SmartContractDeployData data) {

        SmartContractDeploy thriftStruct = new SmartContractDeploy();
        thriftStruct.setSourceCode(data.getSourceCode());
        thriftStruct.setByteCodeObjects(GeneralConverter.byteCodeObjectsDataToByteCodeObjects(data.getByteCodeObjects()));
        thriftStruct.setHashState(data.getHashState());
        thriftStruct.setTokenStandart(data.getTokenStandard());
        return thriftStruct;
    }

    public static SmartContractData smartContractToSmartContractData(SmartContract smartContract) {

        return new SmartContractData(smartContract.getAddress(), smartContract.getDeployer(),
            NodePojoConverter.smartContractDeployToSmartContractDeployData(smartContract.getSmartContractDeploy()),
            smartContract.getObjectState());
    }

    public static SmartContractDeployData smartContractDeployToSmartContractDeployData(
        SmartContractDeploy thriftStruct) {

        return new SmartContractDeployData(thriftStruct.getSourceCode(), GeneralConverter.byteCodeObjectsToByteCodeObjectsData(thriftStruct.getByteCodeObjects()),
            thriftStruct.getTokenStandart());
    }


    public static SmartContractInvocation smartContractInvocationDataToSmartContractInvocation(
        SmartContractInvocationData smartContractInvocationData) {

        List<Variant> params = new ArrayList<>();

        smartContractInvocationData.getParams()
            .forEach(variantData -> params.add(VariantConverter.variantDataToVariant(variantData)));

        SmartContractInvocation thriftStruct =
            new SmartContractInvocation(smartContractInvocationData.getMethod(), params,
                smartContractInvocationData.isForgetNewState());
        SmartContractDeployData smartContractDeployData = smartContractInvocationData.getSmartContractDeployData();
        if (smartContractDeployData != null) {
            thriftStruct.setSmartContractDeploy(
                NodePojoConverter.smartContractDeployDataToSmartContractDeploy(smartContractDeployData));
        }

        return thriftStruct;
    }

    public static Transaction smartContractTransactionFlowDataToTransaction(
        SmartContractTransactionFlowData scTransaction) {

        Transaction transaction = new Transaction();
        transaction.id = scTransaction.getId();
        transaction.source = ByteBuffer.wrap(scTransaction.getSource());
        transaction.target = ByteBuffer.wrap(scTransaction.getTarget());
        transaction.amount = null;
        transaction.balance = null;
        transaction.currency = (byte) 1;
        transaction.signature = ByteBuffer.wrap(scTransaction.getSignature());
        transaction.fee = new AmountCommission(scTransaction.getOfferedMaxFee());
        SmartContractInvocation smartContractInvocation =
            smartContractInvocationDataToSmartContractInvocation(scTransaction.getSmartContractData());
        transaction.setSmartContract(smartContractInvocation);
        transaction.setSmartContractIsSet(true);
        return transaction;
    }

    public static TransactionFlowResultData transactionFlowResultToTransactionFlowResultData(
        TransactionFlowResult result, byte[] source, byte[] target) {
        return new TransactionFlowResultData(apiResponseToApiResponseData(result.getStatus()), result.getRoundNum(),
            source, target, result.getSmart_contract_result());
    }

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse) {
        return new ApiResponseData(ApiResponseCode.valueOf(apiResponse.getCode()), apiResponse.getMessage());
    }

    public static Transaction transactionFlowDataToTransaction(TransactionFlowData transactionData) {
        Transaction transaction = new Transaction();
        transaction.id = transactionData.getId();
        transaction.source = ByteBuffer.wrap(transactionData.getSource());
        transaction.target = ByteBuffer.wrap(transactionData.getTarget());
        transaction.amount = bigDecimalToAmount(transactionData.getAmount());
        transaction.fee = new AmountCommission(transactionData.getOfferedMaxFee());
        transaction.userFields = ByteBuffer.wrap(transactionData.getCommentBytes());
        transaction.signature = ByteBuffer.wrap(transactionData.getSignature());
        return transaction;
    }

    @Deprecated
    public static TransactionId transactionIdDataToTransactionId(TransactionIdData transactionIdData) {
        TransactionId transactionId = new TransactionId();
        transactionId.setPoolHash(byteArrayToByteBuffer(transactionIdData.getPoolHash()));
        transactionId.setIndex(transactionIdData.getIndex());
        return transactionId;
    }

    public static long getShortTransactionId(long wideTransactionId) {
        long maskForZeroingFirstTwoBit = 0x3FFFFFFFFFFFL;
        return wideTransactionId & maskForZeroingFirstTwoBit;
    }
}
