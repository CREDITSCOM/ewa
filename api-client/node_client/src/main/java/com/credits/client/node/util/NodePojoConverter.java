package com.credits.client.node.util;

import com.credits.client.node.pojo.PoolData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.client.node.pojo.SmartContractTransactionFlowData;
import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.pojo.TransactionIdData;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.generated.Amount;
import com.credits.client.node.thrift.generated.AmountCommission;
import com.credits.client.node.thrift.generated.Pool;
import com.credits.client.node.thrift.generated.SealedTransaction;
import com.credits.client.node.thrift.generated.SmartContract;
import com.credits.client.node.thrift.generated.SmartContractInvocation;
import com.credits.client.node.thrift.generated.Transaction;
import com.credits.client.node.thrift.generated.TransactionFlowResult;
import com.credits.client.node.thrift.generated.TransactionId;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Converter;
import com.credits.general.util.exception.ConverterException;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.credits.general.util.Constants.ds;
import static com.credits.general.util.Converter.byteArrayToByteBuffer;
import static com.credits.general.util.Converter.toBigDecimal;

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

        String valueAsString = Converter.toString(value);

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

        Transaction transaction = sealedTransaction.getTrxn();
        TransactionData data = new TransactionData();
        Long innerId = transaction.getId();
        data.setAmount(NodePojoConverter.amountToBigDecimal(transaction.getAmount()));
        data.setCurrency(transaction.getCurrency());
        data.setId(innerId);
        data.setSource(transaction.getSource());
        data.setTarget(transaction.getTarget());
        data.setBalance(NodePojoConverter.amountToBigDecimal(transaction.getBalance()));
        return data;
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
        return data;
    }

    public static BigDecimal amountToBigDecimal(Amount amount) {

        int integralPart = amount.getIntegral();
        BigDecimal fractionPart = toBigDecimal(amount.getFraction()).divide(toBigDecimal("1000000000000000000"), BigDecimal.ROUND_UP);

        integralPart += fractionPart.intValue();
        String integralPartAsString = Converter.toString(integralPart);
        String fractionPartAsString = Converter.toString(fractionPart);


        if (fractionPartAsString.contains(ds)) {
            String[] valueDelimited = fractionPartAsString.split("[" + ds + "]");
            fractionPartAsString = valueDelimited[1];
        } else {
            fractionPartAsString = "0";
        }
        return new BigDecimal(String.format("%s.%s", integralPartAsString, fractionPartAsString));
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
        smartContract.setSourceCode(smartContractData.getSourceCode());
        smartContract.setByteCode(smartContractData.getByteCode());
        smartContract.setHashState(smartContractData.getHashState());
        return smartContract;
    }

    public static SmartContractData smartContractToSmartContractData(SmartContract smartContract) {

        return new SmartContractData(smartContract.getAddress(), smartContract.getDeployer(), smartContract.getSourceCode(),
            smartContract.getByteCode(), smartContract.getHashState(), smartContract.getObjectState());
    }

    public static SmartContractInvocation smartContractInvocationDataToSmartContractInvocation(
        SmartContractInvocationData smartContractInvocationData) {

        List<Variant> params = new ArrayList<>();

        smartContractInvocationData.getParams().forEach(object -> params.add(NodePojoConverter.objectToVariant(object)));

        return new SmartContractInvocation(smartContractInvocationData.getSourceCode(), ByteBuffer.wrap(smartContractInvocationData.getByteCode()),
            smartContractInvocationData.getHashState(), smartContractInvocationData.getMethod(), params,
            smartContractInvocationData.isForgetNewState());
    }

    public static Transaction smartContractTransactionFlowDataToTransaction(SmartContractTransactionFlowData scTransaction){

        Transaction transaction = new Transaction();
        transaction.id = scTransaction.getId();
        transaction.source = ByteBuffer.wrap(scTransaction.getSource());
        transaction.target = ByteBuffer.wrap(scTransaction.getTarget());
        transaction.amount = null;
        transaction.balance = null;
        transaction.currency = (byte) 1;
        transaction.signature = ByteBuffer.wrap(scTransaction.getSignature());
        transaction.fee = new AmountCommission(scTransaction.getOfferedMaxFee());
        SmartContractInvocation smartContractInvocation = smartContractInvocationDataToSmartContractInvocation(scTransaction.getSmartContractData());
        transaction.setSmartContract(smartContractInvocation);
        transaction.setSmartContractIsSet(true);
        return transaction;
    }

    public static Variant objectToVariant(Object object) {
        Class clazz = object.getClass();
        Variant variant = new Variant();
        if (clazz.equals(String.class)) {
            variant.setV_string((String) object);
        } else if (clazz.equals(Integer.class)) {
            variant.setV_i32((Integer) object);
        } else if (clazz.equals(Double.class)) {
            variant.setV_double((Double) object);
        } else if (clazz.equals(Byte.class)) {
            variant.setV_i8((Byte) object);
        } else if (clazz.equals(Short.class)) {
            variant.setV_i16((Short) object);
        } else if (clazz.equals(Long.class)) {
            variant.setV_i64((Long) object);
        } else if (clazz.equals(Boolean.class)) {
            variant.setV_bool((Boolean) object);
        } else if (clazz.equals(List.class)) {
            List objectList = (List) object;
            List<Variant> variantList = new ArrayList<>();
            for (Object obj : objectList) {
                variantList.add(objectToVariant(obj));
            }
            variant.setV_list(variantList);
        }
        return variant;
    }

    public static ApiResponseData transactionFlowResultToApiResponseData(TransactionFlowResult result){
        return new ApiResponseData(ApiResponseCode.valueOf(result.getStatus().getCode()), result.getStatus().message, result.smart_contract_result);
    }

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse, Variant smartContractResult) {
        return new ApiResponseData(ApiResponseCode.valueOf(apiResponse.getCode()), apiResponse.getMessage(), smartContractResult);
    }

    public static Transaction transactionFlowDataToTransaction(TransactionFlowData transactionData){
        Transaction transaction = new Transaction();
        transaction.id = transactionData.getId();
        transaction.source = ByteBuffer.wrap(transactionData.getSource());
        transaction.target = ByteBuffer.wrap(transactionData.getTarget());
        transaction.amount = bigDecimalToAmount(transactionData.getAmount());
        transaction.fee =  new AmountCommission(transactionData.getOfferedMaxFee());
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
