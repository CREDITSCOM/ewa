package com.credits.general.client.node.util;

import com.credits.general.client.node.pojo.ApiResponseData;
import com.credits.general.client.node.pojo.PoolData;
import com.credits.general.client.node.pojo.SmartContractData;
import com.credits.general.client.node.pojo.SmartContractInvocationData;
import com.credits.general.client.node.pojo.TransactionData;
import com.credits.general.client.node.pojo.TransactionIdData;
import com.credits.general.client.node.pojo.WalletData;
import com.credits.general.client.node.thrift.generated.Variant;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.Const;
import com.credits.general.util.Converter;
import com.credits.general.client.node.exception.NodeClientException;
import com.credits.general.client.node.thrift.APIResponse;
import com.credits.general.client.node.thrift.Amount;
import com.credits.general.client.node.thrift.Pool;
import com.credits.general.client.node.thrift.SealedTransaction;
import com.credits.general.client.node.thrift.SmartContract;
import com.credits.general.client.node.thrift.SmartContractInvocation;
import com.credits.general.client.node.thrift.Transaction;
import com.credits.general.client.node.thrift.TransactionId;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class ClientConverter {

    public static Double amountToDouble(Amount amount) {

        int integralPart = amount.getIntegral();
        Double fractionPart = amount.getFraction() / 1000000000000000000D;
        return Double.valueOf(integralPart) + fractionPart;
    }

    public static BigDecimal amountToBigDecimal(Amount amount) throws NodeClientException {

        Integer integralPart = amount.getIntegral();
        BigDecimal fractionPart =
                null;
        try {
            fractionPart = Converter.toBigDecimal(amount.getFraction())
                    .divide(Converter.toBigDecimal("1000000000000000000"));
        } catch (CreditsException e) {
            throw new NodeClientException(e);
        }

        integralPart += fractionPart.intValue();
        String integralPartAsString = Converter.toString(integralPart);
        String fractionPartAsString = Converter.toString(fractionPart);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Const.LOCALE);
        char sep = symbols.getDecimalSeparator();
        String separator = Character.toString(sep);

        if (fractionPartAsString.contains(separator)) {
            String[] valueDelimited = fractionPartAsString.split("\\" + separator);
            fractionPartAsString = valueDelimited[1];
        } else {
            fractionPartAsString = "0";
        }
        return new BigDecimal(String.format("%s.%s", integralPartAsString, fractionPartAsString));
    }

    public static Amount doubleToAmount(Double value) throws NodeClientException {

        if (value == null) {
            throw new NodeClientException("value is null");
        }
        String valueAsString = String.format(Locale.ROOT, "%.18f", value);
        String[] valueDelimited = valueAsString.split("\\.");
        Integer integral = Integer.valueOf(valueDelimited[0]);
        Long fraction = Long.valueOf(valueDelimited[1]);
        return new Amount(integral, fraction);
    }

    public static Amount bigDecimalToAmount(BigDecimal value) throws NodeClientException {

        if (value == null) {
            throw new NodeClientException("value is null");
        }

        Integer integral;

        Long fraction;

        String valueAsString = Converter.toString(value);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Const.LOCALE);
        char sep = symbols.getDecimalSeparator();
        String separator = Character.toString(sep);

        if (valueAsString.contains(separator)) {
            String[] valueDelimited = valueAsString.split("\\" + separator);
            integral = Integer.valueOf(valueDelimited[0]);
            String fractionAsString = String.format("%-18s", valueDelimited[1]).replace(' ', '0');
            fraction = Long.valueOf(fractionAsString);
        } else {
            integral = Integer.valueOf(valueAsString);
            fraction = 0L;
        }
        return new Amount(integral, fraction);
    }


    public static TransactionData transactionToTransactionData(SealedTransaction sealedTransaction) throws NodeClientException {

        Transaction trxn = sealedTransaction.getTrxn();
        TransactionData data = new TransactionData();
        Long innerId = trxn.getId();
        data.setAmount(ClientConverter.amountToBigDecimal(trxn.getAmount()));
        data.setCurrency(trxn.getCurrency());
        data.setId(innerId);
        data.setSource(trxn.getSource());
        data.setTarget(trxn.getTarget());
        data.setBalance(ClientConverter.amountToBigDecimal(trxn.getBalance()));
        return data;
    }

    public static TransactionData transactionToTransactionData(Transaction trxn) throws NodeClientException {
        TransactionData data = new TransactionData();
        Long innerId = trxn.getId();
        if(trxn.getAmount() == null) {
            data.setAmount(BigDecimal.ZERO);
        } else {
            data.setAmount(ClientConverter.amountToBigDecimal(trxn.getAmount()));
        }        data.setCurrency(trxn.getCurrency());
        data.setId(innerId);
        data.setSource(trxn.getSource());
        data.setTarget(trxn.getTarget());
        if(trxn.getBalance() == null) {
            data.setBalance(BigDecimal.ZERO);
        } else {
            data.setBalance(ClientConverter.amountToBigDecimal(trxn.getBalance()));
        }
        return data;
    }


    public static WalletData walletToWalletData(com.credits.general.client.node.thrift.WalletData walletData) throws NodeClientException {

        WalletData data = new WalletData(
                walletData.getWalletId(),
                ClientConverter.amountToBigDecimal(walletData.getBalance()),
                walletData.getLastTransactionId()
        );

        return data;
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

        SmartContractData smartContractData = new SmartContractData(
                smartContract.getAddress(),
                smartContract.getDeployer(),
                smartContract.getSourceCode(),
                smartContract.getByteCode(),
                smartContract.getHashState(),
                smartContract.getObjectState()
        );
        return smartContractData;
    }

    public static SmartContractInvocation smartContractInvocationDataToSmartContractInvocation(SmartContractInvocationData smartContractInvocationData) {

        List<Variant> params = new ArrayList<>();

        smartContractInvocationData.getParams().forEach(object -> {
            params.add(ClientConverter.objectToVariant(object));
        });

        SmartContractInvocation smartContractInvocation = new SmartContractInvocation(
                smartContractInvocationData.getSourceCode(),
                ByteBuffer.wrap(smartContractInvocationData.getByteCode()),
                smartContractInvocationData.getHashState(),
                smartContractInvocationData.getMethod(),
                params,
                smartContractInvocationData.isForgetNewState()
        );

        return smartContractInvocation;
    }

    private static Variant objectToVariant(Object object) {
        Class clazz = object.getClass();
        Object value = object;
        Variant variant = new Variant();
        if (clazz.equals(String.class)) {
            variant.setV_string((String)value);
        } else if (clazz.equals(Integer.class)) {
            variant.setV_i32((Integer)value);
        } else if (clazz.equals(Double.class)) {
            variant.setV_double((Double)value);
        } else if (clazz.equals(Byte.class)) {
            variant.setV_i8((Byte)value);
        } else if (clazz.equals(Short.class)) {
            variant.setV_i16((Short)value);
        } else if (clazz.equals(Long.class)) {
            variant.setV_i64((Long)value);
        } else if (clazz.equals(Boolean.class)) {
            variant.setV_bool((Boolean)value);
        } else if (clazz.equals(List.class)) {
            List objectList = (List)value;
            List<Variant> variantList = new ArrayList();
            objectList.forEach(obj -> {
                variantList.add(objectToVariant(obj));
            });
            variant.setV_list(variantList);
        }
        return variant;
    }

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse) {
        return new ApiResponseData(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    public static ApiResponseData apiResponseToApiResponseData(APIResponse apiResponse, Variant smartContractResult) {
        return new ApiResponseData(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    public static TransactionId transactionIdDataToTransactionId(TransactionIdData transactionIdData) {
        TransactionId transactionId = new TransactionId();
        transactionId.setPoolHash(Converter.byteArrayToByteBuffer(transactionIdData.getPoolHash()));
        transactionId.setIndex(transactionIdData.getIndex());
        return transactionId;
    }

}
