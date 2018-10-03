package com.credits.wallet.desktop.utils.struct;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.thrift.Amount;
import com.credits.leveldb.client.util.LevelDbClientConverter;
import org.apache.commons.lang3.ArrayUtils;

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
    private short offeredMaxFee;
    private byte currency;
    private byte ufNum;
    private int scLen;
    private byte[] sc;

    public TransactionStruct(long id, byte[] source, byte[] target, BigDecimal amount, Short offeredMaxFee, byte currency,
                             byte[] sc)
            throws LevelDbClientException {
        this.id = id;
        this.source = source;
        this.target = target;

        Amount aAmount = LevelDbClientConverter.bigDecimalToAmount(amount);
        this.amountInt=aAmount.integral;
        this.amountFrac=aAmount.fraction;

        this.offeredMaxFee = offeredMaxFee;

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
            byte[] idBytes = Converter.toByteArrayLittleEndian(id, 8);
            idBytes = ArrayUtils.remove(idBytes, 7); // delete two last bytes
            idBytes = ArrayUtils.remove(idBytes, 6);
            os.write(idBytes);
            os.write(source);
            os.write(target);
            os.write(Converter.toByteArrayLittleEndian(amountInt, 4));
            os.write(Converter.toByteArrayLittleEndian(amountFrac, 8));
            os.write(Converter.toByteArrayLittleEndian(offeredMaxFee, 2));
            os.write(Converter.toByteArrayLittleEndian(currency, 1));
            os.write(Converter.toByteArrayLittleEndian(ufNum, 1));
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
