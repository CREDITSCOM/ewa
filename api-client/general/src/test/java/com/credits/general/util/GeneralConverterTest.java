package com.credits.general.util;

import com.credits.general.util.exception.ConverterException;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.BitSet;

import static com.credits.general.util.Constants.ds;


/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
@SuppressWarnings("SpellCheckingInspection")
public class GeneralConverterTest {


    @Test
    public void toStringTest() {
        Assert.assertEquals("1" + ds + "0000000000001", GeneralConverter.toString(1.0000000000001));
        Assert.assertEquals("1000000001", GeneralConverter.toString(1000000001));
        Assert.assertEquals("111111111111111111", GeneralConverter.toString(111111111111111111L));
        Assert.assertEquals("2222222222222222222", GeneralConverter.toString(new BigDecimal(2222222222222222222L)));
        Assert.assertEquals("1", GeneralConverter.toString((byte)1));
    }

    @Test
    public void decodeFromBASE64Test() {
        String text = "KoX94CTSk7v7b28dzN7hXNvYPn8wxn7cStYfY3F7hMn";
        byte[] bytes = GeneralConverter.decodeFromBASE64(text);
        Assert.assertEquals(32, bytes.length);
    }

    @Test
    public void base64EncodeDecodeTest() {
        byte[] data1 = "Hello World!!!".getBytes();
        byte[] data2 = GeneralConverter.decodeFromBASE64(GeneralConverter.encodeToBASE64(data1));
        Assert.assertArrayEquals(data1, data2);
    }

    @Test
    public void decodeFromBASE58Test() throws ConverterException {
        String text = "aumu8afRGeQf5viweyNEuYoZzHvnpCahxoAeonmT5qUm";
        byte[] bytes = GeneralConverter.decodeFromBASE58(text);
        Assert.assertEquals(33, bytes.length);
    }

    @Test
    public void encodeToBASE58Test01() {
        byte[] bytes = {1, 2, 3};
        String text = GeneralConverter.encodeToBASE58(bytes);
        Assert.assertEquals("Ldp", text);
    }


    @Test
    public void base58EncodeDecodeTest() throws ConverterException {
        byte[] data1 = "KoX94CTSk7v7b28dzN7hXNvYPn8wxn7cStYfY3F7hMn".getBytes();
        byte[] data2 = GeneralConverter.decodeFromBASE58(GeneralConverter.encodeToBASE58(data1));
        Assert.assertArrayEquals(data1, data2);
    }

    @Test
    public void base58DecodeTest01() throws ConverterException {
        String base58String = "Ks8XUU7MRbGWGk7CJ5b2fn1zL2zQUoLP7oZUJaAT1UxTjViUxNjfuU9ttQiyBmVsXicoAeeoMMaEGQ5icVnBsXL";
        byte[] bytes = GeneralConverter.decodeFromBASE58(base58String);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(b).append(",");
        }
    }


    @Test
    public void toBigDecimalTest() {
        String longString = "111111111111111111L";
        String intString = "1111111111";
        String doubleString = "1" + ds + "1111111111111111";
        long longValue = GeneralConverter.toBigDecimal(longString).longValue();
        int integerValue = GeneralConverter.toBigDecimal(intString).intValue();
        double doubleValue = GeneralConverter.toBigDecimal(doubleString).setScale(13, BigDecimal.ROUND_DOWN).doubleValue();
        Assert.assertEquals(111111111111111111L, (Object) longValue);
        Assert.assertEquals(1111111111, (Object) integerValue);
        Assert.assertEquals(1.1111111111111, (Object) doubleValue);
    }

    @Test
    public void longToBitSetTest01() {
        Long value = 9223372036854705807L;
        BitSet bitSet = GeneralConverter.toBitSet(value);
        Long outValue = GeneralConverter.toLong(bitSet);
        Assert.assertEquals(value, outValue);
    }

    @Test
    public void toByteArrayLittleEndian01() throws ConverterException {
        Long value = 1000000000L;
        byte[] byteArrayLittleEndian = GeneralConverter.toByteArrayLittleEndian(value, 8);
        Assert.assertArrayEquals(byteArrayLittleEndian, GeneralConverter.toByteArrayLittleEndian(byteArrayLittleEndian, 8));
    }

    @Test
    public void toBooleanTest() {
        String value = "true";
        Assert.assertEquals(GeneralConverter.toBoolean(value), true);
    }

    @Test
    public void longToBytesTest() throws ConverterException {
        Long value = 10001L;
        byte[] bytes = GeneralConverter.toByteArray(value);
        Assert.assertEquals(value, GeneralConverter.toLong(bytes));
    }

    @Test
    public void toIntegerTest() {
        Long value = (long)Integer.MAX_VALUE;
        Assert.assertEquals(GeneralConverter.toInteger(value), (Integer)Integer.MAX_VALUE);
    }

    @Test
    public void toFloatTest() {
        String value = "1";
        Assert.assertEquals(GeneralConverter.toFloat(value), (Float)1F);
    }

    @Test
    public void toByteTest() {
        String value = "1";
        Assert.assertEquals(GeneralConverter.toByte(value), (Byte)(byte)1);
    }

    @Test
    public void toShortTest() {
        String value = "1";
        Assert.assertEquals(GeneralConverter.toShort(value), (Short)(short)1);
    }

    @Test
    public void toLongTest() {
        String value = "1";
        Assert.assertEquals(GeneralConverter.toLong(value), (Long)1L);
    }

    @Test
    public void toCharacterTest() {
        String value = "1";
        Assert.assertEquals(GeneralConverter.toCharacter(value), (Character)'1');
    }

    @Test
    public void toDoubleTest01() {
        String value = "1";
        Assert.assertEquals(GeneralConverter.toDouble(value), (Double)1D);
    }

    @Test
    public void toDoubleTest02() {
        String value = "1" + ds + "2";
        Assert.assertEquals(GeneralConverter.toDouble(value, Constants.LOCALE, GeneralConverter.DOUBLE_FORMAT), (Double)1.2D);
    }
    @Test
    public void toDoubleTest03() {
        Float value = 1.2F;
        Assert.assertEquals(GeneralConverter.toDouble(value), (Double)1.2D);
    }

}
