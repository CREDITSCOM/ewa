package general.tests.crypto;

import com.credits.general.util.Base58;
import com.credits.general.util.exception.ConverterException;
import org.junit.Assert;
import org.junit.Test;

public class Base58Test {

    @Test
    public void encodeTest() {
        byte[] arr = {1, 2, 3};
        String encoded = Base58.encode(arr);
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.isEmpty());
        Assert.assertEquals("Ldp", encoded);

        Assert.assertEquals("", Base58.encode(new byte[]{}));
    }

    @Test
    public void decodeTest() throws ConverterException {
        String testString = "Ldp";
        byte[] decodedArray = Base58.decode(testString);
        Assert.assertNotNull(decodedArray);
        Assert.assertEquals(3, decodedArray.length);
        Assert.assertEquals(1, decodedArray[0]);
        Assert.assertEquals(2, decodedArray[1]);
        Assert.assertEquals(3, decodedArray[2]);

        Assert.assertEquals(0, Base58.decode("").length);
    }
}
