package com.credits.client.node.util;

import com.credits.client.node.pojo.*;
import com.credits.client.node.pojo.WalletData;
import com.credits.client.node.thrift.generated.*;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.exception.ConverterException;
import com.credits.general.util.variant.VariantConverter;
import org.apache.commons.codec.binary.Hex;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.credits.general.util.Constants.ds;
import static com.credits.general.util.GeneralConverter.byteArrayToByteBuffer;
import static com.credits.general.util.GeneralPojoConverter.createApiResponseData;
import static com.credits.general.util.variant.VariantConverter.variantToVariantData;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class NodePojoConverter {

    public static final String BIGDECIMAL_SEPARATOR = ".";

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

        if (valueAsString.contains(BIGDECIMAL_SEPARATOR)) {
            String[] valueDelimited = valueAsString.split("[" + BIGDECIMAL_SEPARATOR + "]");
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
        data.setBlockId(Hex.encodeHexString(blockTransactionId.getPoolHash()) + "." + blockTransactionId.getIndex());
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
        data.setSmartInfo(NodePojoConverter.createSmartTransInfoData(transaction.getSmartInfo()));
        data.setType(createTransactionTypeData(transaction.getType()));
        return data;
    }

    private static List<VariantData> variantListToVariantDataList(List<Variant> params) {
        ArrayList<VariantData> objectParams = new ArrayList<>();
        params.forEach(object -> objectParams.add(variantToVariantData(object)));
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
        data.setType(TransactionTypeData.TT_Normal);
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

    public static SmartTransInfoData createSmartTransInfoData(
            SmartTransInfo thriftStruct) {

        if (thriftStruct.isSetV_smartDeploy()) {
            SmartDeployTransInfo child = thriftStruct.getV_smartDeploy();
            return createSmartDeployTransInfoData(child);
        } else if (thriftStruct.isSetV_smartExecution()) {
            SmartExecutionTransInfo child = thriftStruct.getV_smartExecution();
            return createSmartExecutionTransInfoData(child);
        } else if (thriftStruct.isSetV_smartState()) {
            SmartStateTransInfo child = thriftStruct.getV_smartState();
            return createSmartStateTransInfoData(child);
        } else if (thriftStruct.isSetV_tokenDeploy()) {
            TokenDeployTransInfo child = thriftStruct.getV_tokenDeploy();
            return createTokenDeployTransInfoData(child);
        } else if (thriftStruct.isSetV_tokenTransfer()) {
            TokenTransferTransInfo child = thriftStruct.getV_tokenTransfer();
            return createTokenTransferTransInfoData(child);
        } else {
            throw new ConverterException(String.format("Unsupported value: %s", thriftStruct.toString()));
        }
    }

    public static SmartDeployTransInfoData createSmartDeployTransInfoData(SmartDeployTransInfo thriftStruct) {
        return new SmartDeployTransInfoData(
                createSmartOperationStateData(thriftStruct.getState()),
                createTransactionIdData(thriftStruct.getStateTransaction())
        );
    }

    public static SmartExecutionTransInfoData createSmartExecutionTransInfoData(SmartExecutionTransInfo thriftStruct) {
        return new SmartExecutionTransInfoData(
                thriftStruct.getMethod(),
                variantListToVariantDataList(thriftStruct.getParams()),
                createSmartOperationStateData(thriftStruct.getState()),
                createTransactionIdData(thriftStruct.getStateTransaction())
        );
    }

    public static SmartStateTransInfoData createSmartStateTransInfoData(SmartStateTransInfo thriftStruct) {
        return new SmartStateTransInfoData(
                thriftStruct.isSuccess(),
                amountToBigDecimal(thriftStruct.getExecutionFee()),
                thriftStruct.getReturnValue() == null ? null : variantToVariantData(thriftStruct.getReturnValue()),
                createTransactionIdData(thriftStruct.getStartTransaction())
        );
    }

    public static TokenDeployTransInfoData createTokenDeployTransInfoData(TokenDeployTransInfo thriftStruct) {
        return new TokenDeployTransInfoData(
                thriftStruct.getName(),
                thriftStruct.getCode(),
                createTokenStandartData(thriftStruct.getStandart())
        );
    }

    public static TokenTransferTransInfoData createTokenTransferTransInfoData(TokenTransferTransInfo thriftStruct) {
        return new TokenTransferTransInfoData(
                thriftStruct.getCode(),
                thriftStruct.getSender(),
                thriftStruct.getReceiver(),
                thriftStruct.getAmount()
        );
    }


    public static TokenStandartData createTokenStandartData(TokenStandart thriftStruct) {
        switch (thriftStruct) {
            case CreditsBasic: return TokenStandartData.CreditsBasic;
            case CreditsExtended: return TokenStandartData.CreditsExtended;
            case NotAToken: return TokenStandartData.NotAToken;
            default: throw new ConverterException(String.format("Unsupported value: %s", thriftStruct.getValue()));
        }
    }



    public static TransactionIdData createTransactionIdData(TransactionId thriftStruct) {
        return new TransactionIdData(
                thriftStruct.getPoolHash(),
                thriftStruct.getIndex()
        );
    }

    public static SmartOperationStateData createSmartOperationStateData(SmartOperationState thriftStruct) {
        switch (thriftStruct) {
            case SOS_Failed: return SmartOperationStateData.SOS_Failed;
            case SOS_Pending: return SmartOperationStateData.SOS_Pending;
            case SOS_Success: return SmartOperationStateData.SOS_Success;
            default: throw new ConverterException(String.format("Unsupported value: %s", thriftStruct.getValue()));
        }
    }

    public static SmartContractInvocation createSmartContractInvocation(
        SmartContractInvocationData smartContractInvocationData) {

        List<Variant> params = new ArrayList<>();

        smartContractInvocationData.getParams()
            .forEach(variantData -> params.add(VariantConverter.variantDataToVariant(variantData)));

        SmartContractInvocation thriftStruct =
            new SmartContractInvocation(smartContractInvocationData.getMethod(), params,new ArrayList<>(),smartContractInvocationData.isForgetNewState());//todo add "надо будет передавать потом поля с форм"
        SmartContractDeployData smartContractDeployData = smartContractInvocationData.getSmartContractDeployData();
        if (smartContractDeployData != null) {
            thriftStruct.setSmartContractDeploy(
                NodePojoConverter.smartContractDeployDataToSmartContractDeploy(smartContractDeployData));
        }

        return thriftStruct;
    }

    public static SmartContractInvocationData createSmartContractInvocationData(
            SmartContractInvocation thriftStruct) {

        return new SmartContractInvocationData(
                createSmartContractDeployData(thriftStruct.getSmartContractDeploy()),
                thriftStruct.getMethod(),
                thriftStruct.getParams().stream().map(VariantConverter::variantToVariantData).collect(Collectors.toList()),
                thriftStruct.forgetNewState
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
                thriftStruct.getTrxn().getSmartContract() == null ? null: createSmartContractInvocationData(thriftStruct.getTrxn().getSmartContract())
        );
    }

    public static TransactionFlowResultData transactionFlowResultToTransactionFlowResultData(
        TransactionFlowResult result, byte[] source, byte[] target) {
        return new TransactionFlowResultData(createApiResponseData(result.getStatus()), result.getRoundNum(),
            source, target, result.getSmart_contract_result() == null ? null
            : variantToVariantData(result.getSmart_contract_result()));
    }

    public static Transaction transactionFlowDataToTransaction(TransactionFlowData transactionData) {
        Transaction transaction = new Transaction();
        transaction.id = transactionData.getId();
        transaction.source = ByteBuffer.wrap(transactionData.getSource());
        transaction.target = ByteBuffer.wrap(transactionData.getTarget());
        transaction.amount = bigDecimalToAmount(transactionData.getAmount());
        transaction.fee = new AmountCommission(transactionData.getOfferedMaxFee16Bits());
        if(transactionData.getCommentBytes()!=null) {
            transaction.userFields = ByteBuffer.wrap(transactionData.getCommentBytes());
        }
        if(transactionData.getSignature()!=null) {
            transaction.signature = ByteBuffer.wrap(transactionData.getSignature());
        }
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

    public static TransactionType createTransactionType(TransactionTypeData data) {
        if (data.equals(TransactionTypeData.TT_Normal)) {
            return TransactionType.TT_Normal;
        }
        if (data.equals(TransactionTypeData.TT_SmartDeploy)) {
            return TransactionType.TT_SmartDeploy;
        }
        if (data.equals(TransactionTypeData.TT_SmartExecute)) {
            return TransactionType.TT_SmartExecute;
        }
        if (data.equals(TransactionTypeData.TT_SmartState)) {
            return TransactionType.TT_SmartState;
        }
        throw new ConverterException(String.format("Unsupported value: %s", data.getValue()));
    }

    public static TransactionTypeData createTransactionTypeData(TransactionType thriftStruct) {
        if (thriftStruct.equals(TransactionType.TT_Normal)) {
            return TransactionTypeData.TT_Normal;
        }
        if (thriftStruct.equals(TransactionType.TT_SmartDeploy)) {
            return TransactionTypeData.TT_SmartDeploy;
        }
        if (thriftStruct.equals(TransactionType.TT_SmartExecute)) {
            return TransactionTypeData.TT_SmartExecute;
        }
        if (thriftStruct.equals(TransactionType.TT_SmartState)) {
            return TransactionTypeData.TT_SmartState;
        }
        throw new ConverterException(String.format("Unsupported value: %s", thriftStruct.getValue()));
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
