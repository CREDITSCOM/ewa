package general.tests.pojo;

import org.junit.Test;

import static com.credits.general.pojo.VariantType.BYTE_BOX;
import static com.credits.general.pojo.VariantType.DOUBLE;
import static com.credits.general.pojo.VariantType.DOUBLE_BOX;
import static com.credits.general.pojo.VariantType.LIST;
import static com.credits.general.pojo.VariantType.MAP;
import static com.credits.general.pojo.VariantType.parseVariant;
import static org.junit.Assert.assertEquals;

public class VariantTypeTest {

    @Test
    public void test(){
        assertEquals(BYTE_BOX, parseVariant("Byte"));
        assertEquals(LIST, parseVariant("List"));
        assertEquals(DOUBLE, parseVariant("double"));
        assertEquals(DOUBLE_BOX, parseVariant("Double"));
        assertEquals(MAP, parseVariant("Map"));
    }

}