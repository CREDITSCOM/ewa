package com.credits.wallet.desktop.utils.struct;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.thrift.Amount;
import com.credits.leveldb.client.util.LevelDbClientConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by goncharov-eg on 20.07.2018.
 */
public class TransactionStruct implements Serializable {
    private long id;
    private byte[] source;
    private byte[] target;
    private int amountInt;
    private long amountFrac;
    private int feeInt;
    private long feeFrac;
    private byte currency;
    private int ufNum;
    private int scLen;
    private byte[] sc;

    public TransactionStruct(long id, byte[] source, byte[] target, BigDecimal amount, BigDecimal fee, byte currency,
                             byte[] sc)
            throws LevelDbClientException {
        this.id = id;
        this.source = source;
        this.target = target;

        Amount aAmount = LevelDbClientConverter.bigDecimalToAmount(amount);
        this.amountInt=aAmount.integral;
        this.amountFrac=aAmount.fraction;

        Amount aFee = LevelDbClientConverter.bigDecimalToAmount(fee);
        this.feeInt = aFee.integral;
        this.feeFrac = aFee.fraction;

        this.currency = currency;

        if (sc==null)
            ufNum=0;
        else {
            this.ufNum = 1;
            this.scLen = sc.length;
            this.sc=sc;
        }
    }

    public byte[] getBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(Converter.toByteArrayLittleEndian(id, 8));
            os.write(source);
            os.write(target);
            os.write(Converter.toByteArrayLittleEndian(amountInt, 4));
            os.write(Converter.toByteArrayLittleEndian(amountFrac, 8));
            os.write(Converter.toByteArrayLittleEndian(feeInt, 4));
            os.write(Converter.toByteArrayLittleEndian(feeFrac, 8));
            os.write(Converter.toByteArrayLittleEndian(currency, 1));
            os.write(Converter.toByteArrayLittleEndian(ufNum, 4));
            if (sc!=null) {
                os.write(Converter.toByteArrayLittleEndian(scLen, 4));
                os.write(Converter.toByteArrayLittleEndian(sc, scLen));
            }
        } catch (IOException e) {
            // do nothing - never happen
        }
        return os.toByteArray();
    }

    public int getScLen() {
        return scLen;
    }
}
