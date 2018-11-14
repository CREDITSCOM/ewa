package com.credits.general.util;

import com.credits.general.util.exception.ConverterException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.BitSet;

import static com.credits.general.util.Constants.ds;


/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
@SuppressWarnings("SpellCheckingInspection")
public class ConverterTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ConverterTest.class);


    @Test
    public void toStringTest() {
        Assert.assertEquals("1" + ds + "0000000000001", Converter.toString(1.0000000000001));
        Assert.assertEquals("1000000001", Converter.toString(1000000001));
        Assert.assertEquals("111111111111111111", Converter.toString(111111111111111111L));
        Assert.assertEquals("2222222222222222222", Converter.toString(new BigDecimal(2222222222222222222L)));
    }

    @Test
    public void decodeFromBASE64Test() {
        String text = "KoX94CTSk7v7b28dzN7hXNvYPn8wxn7cStYfY3F7hMn";
        byte[] bytes = Converter.decodeFromBASE64(text);
        Assert.assertEquals(32, bytes.length);
        LOGGER.info(String.valueOf(bytes.length));
    }

    @Test
    public void base64EncodeDecodeTest() {
        byte[] data1 = "Hello World!!!".getBytes();
        LOGGER.info(Arrays.toString(data1));
        byte[] data2 = Converter.decodeFromBASE64(Converter.encodeToBASE64(data1));
        LOGGER.info(Arrays.toString(data2));
        Assert.assertArrayEquals(data1, data2);
    }

    @Test
    public void decodeFromBASE58Test() throws ConverterException {
        String text = "aumu8afRGeQf5viweyNEuYoZzHvnpCahxoAeonmT5qUm";
        byte[] bytes = Converter.decodeFromBASE58(text);
        Assert.assertEquals(33, bytes.length);
        LOGGER.info(String.valueOf(bytes.length));
    }

    @Test
    public void encodeToBASE58Test01() {
        byte[] bytes = {1, 2, 3};
        String text = Converter.encodeToBASE58(bytes);
        Assert.assertEquals("Ldp", text);
        LOGGER.info(text);
    }


    @Test
    public void base58EncodeDecodeTest() throws ConverterException {
        byte[] data1 = "KoX94CTSk7v7b28dzN7hXNvYPn8wxn7cStYfY3F7hMn".getBytes();
        LOGGER.info(Arrays.toString(data1));
        byte[] data2 = Converter.decodeFromBASE58(Converter.encodeToBASE58(data1));
        LOGGER.info(Arrays.toString(data2));
        Assert.assertArrayEquals(data1, data2);
    }

    @Test
    public void base58DecodeTest01() throws ConverterException {
        String base58String = "Ks8XUU7MRbGWGk7CJ5b2fn1zL2zQUoLP7oZUJaAT1UxTjViUxNjfuU9ttQiyBmVsXicoAeeoMMaEGQ5icVnBsXL";
        byte[] bytes = Converter.decodeFromBASE58(base58String);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(b).append(",");
        }
        LOGGER.info(builder.toString());
    }


    @Test
    public void toBigDecimalTest() {
        String longString = "111111111111111111L";
        String intString = "1111111111";
        String doubleString = "1" + ds + "1111111111111111";
        long longValue = Converter.toBigDecimal(longString).longValue();
        int integerValue = Converter.toBigDecimal(intString).intValue();
        double doubleValue = Converter.toBigDecimal(doubleString).setScale(13, BigDecimal.ROUND_DOWN).doubleValue();
        LOGGER.info("Long value = " + longValue);
        LOGGER.info("Integer value = " + integerValue);
        LOGGER.info("Double value = " + doubleValue);
        Assert.assertEquals(111111111111111111L, (Object) longValue);
        Assert.assertEquals(1111111111, (Object) integerValue);
        Assert.assertEquals(1.1111111111111, (Object) doubleValue);
    }

    @Test
    public void longToBitSetTest01() {
        long value = 9223372036854705807L;
        System.out.println("value = " + value);
        BitSet bitSet = Converter.toBitSet(value);
        for (int i = 0; i < bitSet.size(); i++) {
            System.out.print((bitSet.get(i) ? "1" : "0") + ",");
        }
    }

    @Test
    public void bitSetToLongTest01() {
        long value = 10000L;
        System.out.println("value = " + value);
        BitSet bitSet = Converter.toBitSet(value);
        for (int i = 0; i < bitSet.size(); i++) {
            System.out.print((bitSet.get(i) ? "1" : "0") + ",");
        }
        bitSet.set(63, true);
        System.out.println();
        for (int i = 0; i < bitSet.size(); i++) {
            System.out.print((bitSet.get(i) ? "1" : "0") + ",");
        }
        System.out.println();
        System.out.println(Converter.toLong(bitSet));
    }

    @Test
    public void toByteArrayLittleEndian01() throws ConverterException {
        Long value = 1000000000L;
        PrintOut.printBytes(Converter.toByteArrayLittleEndian(value, 8));
        PrintOut.printBytes(Converter.toByteArray(value));
    }

    @Test
    public void toBooleanTest() {
        String value = "true";
        LOGGER.info(Converter.toBoolean(value) + "");
    }

    @Test
    public void longToBytesTest() throws ConverterException {
        Long value = 10001L;
        byte[] bytes = Converter.toByteArray(value);
        PrintOut.printBytes(bytes);
    }

    @Test
    public void bigDecimalToShortTest() {
        Short value = 0x6648;
        PrintOut.printBytes(Converter.toByteArrayLittleEndian(value, 2));
    }

    @Test
    public void toIntegerTest() {
        String value = "1";
        LOGGER.info(Converter.toInteger(value) + "");
    }

    @Test
    public void toFloatTest() {
        String value = "1";
        LOGGER.info(Converter.toFloat(value) + "");
    }

    @Test
    public void toByteTest() {
        String value = "1";
        LOGGER.info(Converter.toByte(value) + "");
    }

    @Test
    public void toShortTest() {
        String value = "1";
        LOGGER.info(Converter.toShort(value) + "");
    }

    @Test
    public void toLongTest() {
        String value = "1";
        LOGGER.info(Converter.toLong(value) + "");
    }

    @Test
    public void toCharacterTest() {
        String value = "1";
        LOGGER.info(Converter.toCharacter(value) + "");
    }

    @Test
    public void toDoubleTest01() {
        String value = "1";
        LOGGER.info(Converter.toDouble(value) + "");
    }

    @Test
    public void toDoubleTest02() {
        String value = "1" + ds + "2";
        try {
            LOGGER.info(Converter.toDouble(value, Constants.LOCALE, Converter.DOUBLE_FORMAT) + "");
        } catch (ConverterException e) {
            e.printStackTrace();
        }
    }
}
