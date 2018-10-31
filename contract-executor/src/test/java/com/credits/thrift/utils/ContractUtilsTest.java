package com.credits.thrift.utils;

import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.exception.CompilationException;
import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;

public class ContractUtilsTest extends ServiceTest {

    private String sourceCodeWithVariables = "import java.util.ArrayList;\n" +
        "import java.util.HashMap;\n" +
        "import java.util.HashSet;\n" +
        "import java.util.List;\n" +
        "import java.util.Map;\n" +
        "import java.util.Set;\n" +
        "\n" +
        "public class Contract extends SmartContract {\n" +
        "    public int intField;\n" +
        "    public Integer integerField;\n" +
        "    public Double doubleField;\n" +
        "    public String stringField;\n" +
        "    public List<Integer> listIntegerField;\n" +
        "    public Set<Integer> setIntegerField;\n" +
        "    public Map<String, Integer> mapStringIntegerField;\n" +
        "\n" +
        "    public Contract() {\n" +
        "        this.intField = 5;\n" +
        "        this.integerField = 55;\n" +
        "        this.doubleField = 5.55;\n" +
        "        this.stringField = \"some string value\";\n" +
        "        this.listIntegerField = new ArrayList<>();\n" +
        "        this.listIntegerField.add(5);\n" +
        "        this.setIntegerField = new HashSet<>();\n" +
        "        this.setIntegerField.add(5);\n" +
        "        this.mapStringIntegerField = new HashMap<>();\n" +
        "        this.mapStringIntegerField.put(\"string key\", 5);\n" +
        "    }\n" +
        "}";

    private String sourceCodeWithoutVariables = "public class Contract extends SmartContract {\n" +
        "    public Contract() {\n" +
        "    }\n" +
        "}";

    private Object instanceWithVariables;
    private Object instanceWithoutVariables;

    @Before
    @Override
    public void setUp() throws Exception {
        instanceWithVariables = getInstance(sourceCodeWithVariables);
        instanceWithoutVariables = getInstance(sourceCodeWithoutVariables);
    }

    @Test
    public void getContractVariablesTest() throws ContractExecutorException {
        Map<String, Variant> map = ContractUtils.getContractVariables(instanceWithVariables);
        Assert.assertNotNull(map);
        Assert.assertEquals(5, map.get("intField").getFieldValue());
        Assert.assertEquals(55, map.get("integerField").getFieldValue());
        Assert.assertEquals(5.55, map.get("doubleField").getFieldValue());
        Assert.assertEquals("some string value", map.get("stringField").getFieldValue());
        Assert.assertEquals(5, ((Variant)((List)map.get("listIntegerField").getFieldValue()).get(0)).getFieldValue());
        Assert.assertTrue(((Set)map.get("setIntegerField").getFieldValue()).contains(new Variant(Variant._Fields.V_I32, 5)));
        Assert.assertEquals(new Variant(Variant._Fields.V_I32, 5),
            ((Map)map.get("mapStringIntegerField").getFieldValue()).get(new Variant(Variant._Fields.V_STRING, "string key")));

        //Checks returning null if no public variables exist in the contract
        Assert.assertEquals(new Variant(Variant._Fields.V_STRING,""),ContractUtils.getContractVariables(instanceWithoutVariables).get("initiator"));
    }

    private Object getInstance(String source) throws CompilationException, IllegalAccessException, InstantiationException {
        byte[] byteCode = compile(source, "Contract", "TKN");
        Class<?> clazz = new ByteArrayContractClassLoader().buildClass(byteCode);
        return clazz.newInstance();
    }
}
