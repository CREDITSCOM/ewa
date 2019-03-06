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

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import static com.credits.client.node.thrift.generated.TransactionType.TT_Normal;
import static com.credits.client.node.util.NodePojoConverter.amountToBigDecimal;
import static com.credits.client.node.util.NodePojoConverter.amountToDouble;
import static com.credits.client.node.util.NodePojoConverter.bigDecimalToAmount;
import static com.credits.client.node.util.NodePojoConverter.createTransactionData;
import static com.credits.client.node.util.NodePojoConverter.doubleToAmount;
import static com.credits.client.node.util.NodePojoConverter.walletToWalletData;
import static com.credits.general.util.GeneralConverter.toBigDecimal;

/**
 * Created by Rustem.Saidaliyev on 08.02.2018.
 */
public class NodePojoConverterTest {

    @Test
    public void bigDecimalToAmountTest1() {
        String valueAsString = "1.1111111111111";
        BigDecimal value = toBigDecimal(valueAsString);
        Amount amount = bigDecimalToAmount(value);
        Assert.assertEquals(amount.getIntegral(), 1);
        Assert.assertEquals(amount.getFraction(), 111111111111100000L);
        valueAsString = "10.0";
        value = toBigDecimal(valueAsString);
        amount = bigDecimalToAmount(value);
        Assert.assertEquals(amount.getIntegral(), 10);
        Assert.assertEquals(amount.getFraction(), 0L);
    }

    @Test
    public void bigDecimalToAmountTest2() {
        String valueAsString = "0.1000000000000000055511151231257827021181583404541015625" ;
        BigDecimal value;
        value = toBigDecimal(valueAsString);
        Amount amount = bigDecimalToAmount(value);
        Assert.assertEquals(amount.getIntegral(), 0);
        Assert.assertEquals(amount.getFraction(), 100000000000000006L);
    }

    @Test
    public void amountToBigDecimalTest1(){
        int integral = 1111111111;
        long fraction = 999999999999999999L;
        Amount amount = new Amount(integral, fraction);
        BigDecimal bigDecimalValue = amountToBigDecimal(amount);
        Assert.assertEquals((Object) 1111111111.999999999999999999, bigDecimalValue.doubleValue());
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
                0,
                TT_Normal
        );
        SealedTransaction sealedTransaction = new SealedTransaction(transactionId, transaction);
        TransactionData transactionData = createTransactionData(sealedTransaction);
        Assert.assertEquals(
                transactionData.toString(),
                "TransactionData{id=0, blockId='706f6f6c48617368.0', source=[115, 111, 117, 114, 99, 101], target=[116, 97, 114, 103, 101, 116], amount=0.0, currency=1, commentBytes=null, method='null', params=null, type=TT_Normal, smartInfo=null}"
        );
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
                0,
                TT_Normal
        );
        TransactionData transactionData = createTransactionData(transaction);
        Assert.assertEquals(
                transactionData.toString(),
                "TransactionData{id=0, blockId='null', source=[115, 111, 117, 114, 99, 101], target=[116, 97, 114, 103, 101, 116], amount=0.0, currency=1, commentBytes=null, method='null', params=null, type=TT_Normal, smartInfo=null}"
        );
    }

    @Test
    public void walletToWalletDataTest() {
        WalletData walletDataThrift = new WalletData(
                0,
                new Amount(0, 0),
                0L
        );
        com.credits.client.node.pojo.WalletData walletData = walletToWalletData(walletDataThrift);
        Assert.assertNotNull(walletData);
    }

    @Test
    public void doubleToAmountTest() throws ConverterException {
        Amount amount = doubleToAmount(99.999);
        Assert.assertEquals(amount.getIntegral(), 99);
        Assert.assertEquals(amount.getFraction(), 999000000000000000L);
    }

    @Test
    public void amountToDoubleTest() {
        Amount amount = new Amount(0, 390000000000000L);
        Assert.assertEquals(amountToDouble(amount), (Object)0.00039D);
    }
}
