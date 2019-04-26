package general.tests.crypto;

import com.credits.general.crypto.exception.CryptoException;
import org.junit.Test;

import static com.credits.general.crypto.Blake2S.generateHash;
import static com.credits.general.util.GeneralConverter.byteArrayToHex;
import static org.junit.Assert.assertEquals;

/**
 * Created by Rustem Saidaliyev on 27-Mar-18.
 */
public class Blake2STest {
    @Test
    public void generateHashTest() throws CryptoException {
        byte[] hash = generateHash(new byte[]{(byte) 0x99, (byte) 0xc2, (byte) 0xd1, (byte) 0x6c, (byte) 0xc1,
                (byte) 0x22, (byte) 0x15, (byte) 0x2b, (byte) 0xac, (byte) 0x27, (byte) 0x61, (byte) 0xe3, (byte) 0x72,
                (byte) 0x86, (byte) 0x1d, (byte) 0x68,
                (byte) 0xb9, (byte) 0x04, (byte) 0xc7, (byte) 0xb3, (byte) 0xc5, (byte) 0x79, (byte) 0x60, (byte) 0x58,
                (byte) 0x12, (byte) 0x31, (byte) 0xe5, (byte) 0xfa, (byte) 0x97, (byte) 0x05, (byte) 0x38, (byte) 0x76,});
        assertEquals(32, hash.length);
        assertEquals("7F8031239A0500B97E395C050BFAFA0ACBA7E95424105983AD7B9E25ED478C74", byteArrayToHex(hash));
    }
}
