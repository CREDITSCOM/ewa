package com.credits.client.node.util;

import com.credits.client.node.pojo.*;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.generated.*;
import com.credits.general.pojo.ExecuteByteCodeResultData;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.GeneralPojoConverter;
import com.credits.general.util.exception.ConverterException;
import com.credits.general.util.variant.VariantConverter;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.credits.general.util.Constants.ds;
import static com.credits.general.util.GeneralConverter.byteArrayToByteBuffer;
import static com.credits.general.util.GeneralPojoConverter.createApiResponseData;
import static com.credits.general.util.GeneralPojoConverter.createExecuteByteCodeResultData;

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

    public static TransactionData createTransactionData(SealedTransaction sealedTransaction) {

        TransactionId blockTransactionId = sealedTransaction.getId();
        Transaction transaction = sealedTransaction.getTrxn();

        TransactionData data = new TransactionData();
        Long innerId = transaction.getId();
        data.setBlockId(
            GeneralConverter.encodeToBASE58(blockTransactionId.getPoolHash()) + "." + blockTransactionId.getIndex());
        data.setAmount(NodePojoConverter.amountToBigDecimal(transaction.getAmount()));
        data.setCurrency(transaction.getCurrency());
        data.setId(innerId);
        data.setSource(transaction.getSource());
        data.setTarget(transaction.getTarget());
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

    public static TransactionData createTransactionData(Transaction transaction) {
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
        thriftStruct.setByteCodeObjects(
            GeneralConverter.byteCodeObjectsDataToByteCodeObjects(data.getByteCodeObjects()));
        thriftStruct.setHashState(data.getHashState());
        thriftStruct.setTokenStandart(tokenStandartDataToTokenStandart(data.getTokenStandardData()));
        return thriftStruct;
    }

    public static SmartContractData smartContractToSmartContractData(SmartContract smartContract) {

        return new SmartContractData(smartContract.getAddress(), smartContract.getDeployer(),
            NodePojoConverter.createSmartContractDeployData(smartContract.getSmartContractDeploy()),
            smartContract.getObjectState());
    }

    public static SmartContractDeployData createSmartContractDeployData(
        SmartContractDeploy thriftStruct) {
        return new SmartContractDeployData(thriftStruct.getSourceCode(),
            GeneralConverter.byteCodeObjectsToByteCodeObjectsData(thriftStruct.getByteCodeObjects()),
            tokenStandartToTokenStandartData(thriftStruct.getTokenStandart()));
    }


    public static SmartContractInvocation createSmartContractInvocation(
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

        ExecuteByteCodeResultData executeResult = smartContractInvocationData.getExecuteResult();
        if (executeResult != null) {
            thriftStruct.setExecuteResult(
                    GeneralPojoConverter.createExecuteByteCodeResult(executeResult));
        }
        return thriftStruct;
    }

    public static SmartContractInvocationData createSmartContractInvocationData(
            SmartContractInvocation thriftStruct) {

        return new SmartContractInvocationData(
                createSmartContractDeployData(thriftStruct.getSmartContractDeploy()),
                thriftStruct.getMethod(),
                thriftStruct.getParams().stream().map(VariantConverter::variantToVariantData).collect(Collectors.toList()),
                thriftStruct.forgetNewState,
                (thriftStruct.getExecuteResult() == null ? null : createExecuteByteCodeResultData(thriftStruct.getExecuteResult()))
        );
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
        transaction.fee = new AmountCommission(scTransaction.getOfferedMaxFee16Bits());
        SmartContractInvocation smartContractInvocation =
            createSmartContractInvocation(scTransaction.getSmartContractData());
        transaction.setSmartContract(smartContractInvocation);
        transaction.setSmartContractIsSet(true);
        return transaction;
    }

    public static SmartContractTransactionData createSmartContractTransactionData(
            SealedTransaction thriftStruct) {
        TransactionData transactionData = createTransactionData(thriftStruct);
        return new SmartContractTransactionData(
                transactionData,
                createSmartContractInvocationData(thriftStruct.getTrxn().getSmartContract())
        );
    }

    public static TransactionFlowResultData transactionFlowResultToTransactionFlowResultData(
        TransactionFlowResult result, byte[] source, byte[] target) {
        return new TransactionFlowResultData(createApiResponseData(result.getStatus()), result.getRoundNum(),
            source, target, result.getSmart_contract_result() == null ? null
            : VariantConverter.variantToVariantData(result.getSmart_contract_result()));
    }

    public static Transaction transactionFlowDataToTransaction(TransactionFlowData transactionData) {
        Transaction transaction = new Transaction();
        transaction.id = transactionData.getId();
        transaction.source = ByteBuffer.wrap(transactionData.getSource());
        transaction.target = ByteBuffer.wrap(transactionData.getTarget());
        transaction.amount = bigDecimalToAmount(transactionData.getAmount());
        transaction.fee = new AmountCommission(transactionData.getOfferedMaxFee16Bits());
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

    public static TokenStandart tokenStandartDataToTokenStandart(TokenStandartData tokenStardartData) {
        if (tokenStardartData.equals(TokenStandartData.NotAToken)) {
            return TokenStandart.NotAToken;
        }
        if (tokenStardartData.equals(TokenStandartData.CreditsBasic)) {
            return TokenStandart.CreditsBasic;
        }
        if (tokenStardartData.equals(TokenStandartData.CreditsExtended)) {
            return TokenStandart.CreditsExtended;
        }
        throw new ConverterException(String.format("Unsupported value: %s", tokenStardartData.getValue()));
    }

    public static TokenStandartData tokenStandartToTokenStandartData(TokenStandart tokenStardart) {
        if (tokenStardart.equals(TokenStandart.NotAToken)) {
            return TokenStandartData.NotAToken;
        }
        if (tokenStardart.equals(TokenStandart.CreditsBasic)) {
            return TokenStandartData.CreditsBasic;
        }
        if (tokenStardart.equals(TokenStandart.CreditsExtended)) {
            return TokenStandartData.CreditsExtended;
        }
        throw new ConverterException(String.format("Unsupported value: %s", tokenStardart.getValue()));
    }

    public static TransactionState transactionStateDataToTransactionState(TransactionStateData transactionStateData) {
        if (transactionStateData.equals(TransactionStateData.INVALID)) {
            return TransactionState.INVALID;
        }
        if (transactionStateData.equals(TransactionStateData.VALID)) {
            return TransactionState.VALID;
        }
        if (transactionStateData.equals(TransactionStateData.INPROGRESS)) {
            return TransactionState.INPROGRESS;
        }
        throw new ConverterException(String.format("Unsupported value: %s", transactionStateData.getValue()));
    }

    public static TransactionStateData transactionStateToTransactionStateData(TransactionState transactionState) {
        if (transactionState.equals(TransactionState.INVALID)) {
            return TransactionStateData.INVALID;
        }
        if (transactionState.equals(TransactionState.VALID)) {
            return TransactionStateData.VALID;
        }
        if (transactionState.equals(TransactionState.INPROGRESS)) {
            return TransactionStateData.INPROGRESS;
        }
        throw new ConverterException(String.format("Unsupported value: %s", transactionState.getValue()));
    }

    public static TransactionsStateGetResultData createTransactionsStateGetResultData(
        TransactionsStateGetResult result) {
        return new TransactionsStateGetResultData(createApiResponseData(result.getStatus()), result.getStates()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> transactionStateToTransactionStateData(e.getValue()))),
            result.getRoundNum());
    }
}
