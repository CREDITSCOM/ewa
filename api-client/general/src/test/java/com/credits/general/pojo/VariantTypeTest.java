package com.credits.general.pojo;

import com.credits.general.thrift.generated.Variant;
import org.junit.Test;

import static com.credits.general.pojo.VariantType.*;
import static org.junit.Assert.*;

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