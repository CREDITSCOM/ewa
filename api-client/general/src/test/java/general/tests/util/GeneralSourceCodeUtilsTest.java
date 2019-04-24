package general.tests.util;

import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class GeneralSourceCodeUtilsTest {

    @Test
    public void splitClassnameAndGenericTest() {
        Pair<String, String> pair = GeneralSourceCodeUtils.splitClassnameAndGeneric("List<Integer>");
        Assert.assertEquals(pair.getLeft(), "List");
        Assert.assertEquals(pair.getRight(), "Integer");
    }

    @Test
    public void parseArrayTypeTest() {
        Assert.assertEquals(GeneralSourceCodeUtils.parseArrayType("double[]"), "double");
    }
}
