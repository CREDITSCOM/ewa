package com.credits.client.node.util;

import com.credits.client.node.pojo.TransactionData;
import com.credits.client.node.thrift.generated.Amount;
import com.credits.client.node.thrift.generated.AmountCommission;
import com.credits.client.node.thrift.generated.SealedTransaction;
import com.credits.client.node.thrift.generated.Transaction;
import com.credits.client.node.thrift.generated.TransactionId;
import com.credits.client.node.thrift.generated.WalletData;
import com.credits.general.util.exception.ConverterException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import static com.credits.client.node.util.NodePojoConverter.amountToBigDecimal;
import static com.credits.client.node.util.NodePojoConverter.amountToDouble;
import static com.credits.client.node.util.NodePojoConverter.bigDecimalToAmount;
import static com.credits.client.node.util.NodePojoConverter.doubleToAmount;
import static com.credits.client.node.util.NodePojoConverter.transactionToTransactionData;
import static com.credits.client.node.util.NodePojoConverter.walletToWalletData;
import static com.credits.general.util.Converter.toBigDecimal;

/**
 * Created by Rustem.Saidaliyev on 08.02.2018.
 */
public class NodePojoConverterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodePojoConverterTest.class);

    @Test
    public void bigDecimalToAmountTest1() throws ConverterException {
        String valueAsString = "1.1111111111111";
        BigDecimal value = toBigDecimal(valueAsString);
        LOGGER.info("bigDecimalToAmountTest, {}", bigDecimalToAmount(value));
        Assert.assertEquals(1, value.intValue());
        Assert.assertEquals(1, value.longValue());
    }

    @Test
    public void bigDecimalToAmountTest2() throws ConverterException {
            String valueAsString = "0.1000000000000000055511151231257827021181583404541015625" ;
            BigDecimal value;
            value = toBigDecimal(valueAsString);
            LOGGER.info("bigDecimalToAmountTest, {}", bigDecimalToAmount(value));
    }

    @Test
    public void amountToBigDecimalTest1(){
        int integral = 1111111111;
        long fraction = 999999999999999999L;
        Amount amount = new Amount(integral, fraction);
        BigDecimal bigDecimalValue = amountToBigDecimal(amount);
        LOGGER.info("amountToBigDecimalTest, {}", bigDecimalValue);
        Assert.assertEquals((Object) 1111111111.999999999999999999, bigDecimalValue.doubleValue());
    }

    @Test
    public void amountToBigDecimalTest2() {
        int integral = 1111111111;
        long fraction = 999999999999999999L;
        Amount amount = new Amount(integral, fraction);
        LOGGER.info("amountToBigDecimalTest, {}", amountToBigDecimal(amount));
    }

    @Test
    public void transactionToTransactionDataTest01() {
        TransactionId transactionId = new TransactionId(ByteBuffer.wrap("poolHash".getBytes()), 0);
        Transaction transaction = new Transaction(
                0,
                ByteBuffer.wrap("source".getBytes()),
                ByteBuffer.wrap("target".getBytes()),
                new Amount(0,0),
                new Amount(0,0),
                (byte)1,
                ByteBuffer.wrap("signature".getBytes()),
                new AmountCommission((short)1),
                0
        );
        SealedTransaction sealedTransaction = new SealedTransaction(transactionId, transaction);
        TransactionData transactionData = transactionToTransactionData(sealedTransaction);
        LOGGER.info(transactionData.toString());
    }

    @Test
    public void transactionToTransactionDataTest02() {
        Transaction transaction = new Transaction(
                0,
                ByteBuffer.wrap("source".getBytes()),
                ByteBuffer.wrap("target".getBytes()),
                new Amount(0,0),
                new Amount(0,0),
                (byte)1,
                ByteBuffer.wrap("signature".getBytes()),
                new AmountCommission((short)1),
                0
        );
        TransactionData transactionData = transactionToTransactionData(transaction);
        LOGGER.info(transactionData.toString());
    }

    @Test
    public void walletToWalletDataTest() {
        WalletData walletDataThrift = new WalletData(
                0,
                new Amount(0, 0),
                0L
        );
        com.credits.client.node.pojo.WalletData walletData = walletToWalletData(walletDataThrift);
        LOGGER.info(walletData.toString());
    }

    @Test
    public void doubleToAmountTest() throws ConverterException {
        LOGGER.info("doubleToAmountTest, {}", doubleToAmount(99.999));
    }

    @Test
    public void amountToDoubleTest() {
        Amount amount = new Amount(0, 390000000000000L);
        LOGGER.info("amountToDoubleTest, {}", amountToDouble(amount));
    }
}
