package com.credits.wallet.desktop.utils.struct;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.thrift.Amount;
import com.credits.leveldb.client.util.LevelDbClientConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by goncharov-eg on 20.07.2018.
 */
public class TransactionStruct implements Serializable {
    private long innerId;
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

    public TransactionStruct(long innerId, String source, String target, BigDecimal amount, BigDecimal fee, byte currency,
                             byte[] sc)
            throws CreditsCommonException, LevelDbClientException {
        this.innerId = innerId;
        this.source = Converter.decodeFromBASE58(source);
        this.target = Converter.decodeFromBASE58(target);

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


            os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(innerId).array());
            os.write(source);
            os.write(target);
            os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(amountInt).array());
            os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(amountFrac).array());
            os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(feeInt).array());
            os.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(feeFrac).array());
            os.write(ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN).put(currency).array());
            os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ufNum).array());
            if (sc!=null) {
                os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(scLen).array());
                os.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(sc).array());
            }
        } catch (IOException e) {
            // do nothing - never happen
        }
        return os.toByteArray();
    }
}
