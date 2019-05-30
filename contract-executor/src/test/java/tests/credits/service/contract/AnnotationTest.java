package tests.credits.service.contract;

import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tests.credits.service.ServiceTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AnnotationTest extends ServiceTest {

    public AnnotationTest() {
        super("/annotationTest/AnnotationTest.java");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    public void get_methods_of_contract() throws Exception {
        AnnotationData getterAnnotation = new AnnotationData("com.credits.scapi.annotations.Getter", new HashMap<>());

        var initialize = createInitializeMethodDescriptionData(getterAnnotation);
        var addTokens = createAddTokensMethodDescriptionData(getterAnnotation);
        var getTotal = createGetTotalMethodDescriptionData();
        var addToken = createAddTokenMethodDescriptionData(getterAnnotation);
        var testToken = createTestTokenMethodDescriptionData();
        var testNotToken = createTestNotTokenMethodDescriptionData();
        var testMultiple1 = createTestMultiple1MethodDescriptionData();
        var testMultiple2 = createTestMultiple2MethodDescriptionData();


        List<MethodDescriptionData> contractsMethods = ceService.getContractsMethods(byteCodeObjectDataList);
        Assert.assertEquals(initialize, findMethod(contractsMethods, "initialize"));
        Assert.assertEquals(addTokens, findMethod(contractsMethods, "addTokens"));
        Assert.assertEquals(getTotal, findMethod(contractsMethods, "getTotal"));
        Assert.assertEquals(addToken, findMethod(contractsMethods, "addToken"));
        Assert.assertEquals(testToken, findMethod(contractsMethods, "testToken"));
        Assert.assertEquals(testNotToken, findMethod(contractsMethods, "testNotToken"));
        Assert.assertEquals(testMultiple1, findMethod(contractsMethods, "testMultiple1"));
        Assert.assertEquals(testMultiple2, findMethod(contractsMethods, "testMultiple2"));
    }

    private MethodDescriptionData createTestMultiple1MethodDescriptionData() {
        ArrayList<AnnotationData> addTokensAnnotationData = new ArrayList<>();
        addTokensAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.UsingContract", new HashMap<>() {{
            put("address", "test1");
            put("method", "notGet");
        }}));
        addTokensAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.UsingContract", new HashMap<>() {{
            put("address", "test2");
            put("method", "notGetBalance");
        }}));


        return new MethodDescriptionData("void", "testMultiple1",
                                         Collections.singletonList(new MethodArgumentData("int", "amount", new ArrayList<>())),
                                         addTokensAnnotationData);
    }

    private MethodDescriptionData createTestMultiple2MethodDescriptionData() {
        ArrayList<AnnotationData> addTokensAnnotationData = new ArrayList<>();
        addTokensAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.UsingContract", new HashMap<>() {{
            put("address", "test3");
            put("method", "notGetA");
        }}));
        addTokensAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.UsingContract", new HashMap<>() {{
            put("address", "test2");
            put("method", "notGetBalance");
        }}));


        return new MethodDescriptionData("void", "testMultiple2",
                                         Collections.singletonList(new MethodArgumentData("int", "amount", new ArrayList<>())),
                                         addTokensAnnotationData);
    }


    public MethodDescriptionData createInitializeMethodDescriptionData(AnnotationData getterAnnotation) {
        return new MethodDescriptionData("void", "initialize", new ArrayList<>(),
                                         Collections.singletonList(getterAnnotation));
    }

    public MethodDescriptionData createAddTokensMethodDescriptionData(AnnotationData getterAnnotation) {
        ArrayList<AnnotationData> addTokensAnnotationData = new ArrayList<>();
        addTokensAnnotationData.add(getterAnnotation);
        addTokensAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.UsingContract", new HashMap<>() {{
            put("address", "test2");
            put("method", "notGetBalance");
        }}));


        return new MethodDescriptionData("void", "addTokens",
                                         Collections.singletonList(new MethodArgumentData("int", "amount", new ArrayList<>())),
                                         addTokensAnnotationData);
    }

    public MethodDescriptionData createGetTotalMethodDescriptionData() {
        return new MethodDescriptionData("int", "getTotal", new ArrayList<>(), new ArrayList<>());
    }

    public MethodDescriptionData createAddTokenMethodDescriptionData(AnnotationData getterAnnotation) {
        ArrayList<AnnotationData> addTokenAnnotationData = new ArrayList<>();
        addTokenAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.UsingContract", new HashMap<>() {{
            put("address", "test1");
            put("method", "getBalance");
        }}));

        return new MethodDescriptionData("void", "addToken",
                                         Collections.singletonList(new MethodArgumentData(
                                             "int",
                                             "amount",
                                             Collections.singletonList(getterAnnotation))),
                                         addTokenAnnotationData);
    }

    public MethodDescriptionData createTestTokenMethodDescriptionData() {
        ArrayList<AnnotationData> testTokenAnnotationData = new ArrayList<>();
        testTokenAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.ContractAddress", new HashMap<>() {{
            put("id", "0");
        }}));
        return new MethodDescriptionData(
            "void",
            "testToken",
            Collections.singletonList(new MethodArgumentData("int", "amount", testTokenAnnotationData)),
            new ArrayList<>());
    }

    public MethodDescriptionData createTestNotTokenMethodDescriptionData() {
        ArrayList<AnnotationData> testNotTokenAnnotationData = new ArrayList<>();
        testNotTokenAnnotationData.add(new AnnotationData("com.credits.scapi.annotations.ContractMethod", new HashMap<>() {{
            put("id", "0");
        }}));

        return new MethodDescriptionData(
            "void",
            "testNotToken",
            Collections.singletonList(new MethodArgumentData("int", "amount", testNotTokenAnnotationData)),
            new ArrayList<>());
    }

    private MethodDescriptionData findMethod(List<MethodDescriptionData> contractsMethods, String name) {
        for (MethodDescriptionData contractsMethod : contractsMethods) {
            if (contractsMethod.name.equals(name)) {
                return contractsMethod;
            }
        }
        return null;
    }


}
