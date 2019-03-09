package com.credits.service.contract;

import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.service.ServiceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.credits.utils.ContractExecutorServiceUtils.parseAnnotationData;


public class AnnotationTest extends ServiceTest {

    public AnnotationTest() {
        super("/annotationTest/AnnotationTest.java");
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    public void main() {
        String getter = "@Getter()";
        String contractAnn = "@Contract(address=test2, method=notGetBalance)";
        String contractMethod = "@ContractMethod(id = 0)";
        AnnotationData annotationData = parseAnnotationData(getter);
        AnnotationData annotationData1 = parseAnnotationData(contractAnn);
        AnnotationData annotationData2 = parseAnnotationData(contractMethod);
        Assert.assertEquals(annotationData, new AnnotationData("Getter", new HashMap<>()));
        Assert.assertEquals(annotationData1, new AnnotationData("Contract", new HashMap<String, String>() {{
            put("address", "test2");
            put("method", "notGetBalance");
        }}));
    }

    @Test
    public void get_methods_of_contract() throws Exception {
        AnnotationData getterAnnotation = new AnnotationData("Getter", new HashMap<>());

        MethodDescriptionData initialize = createInitializeMethodDescriptionData(getterAnnotation);
        MethodDescriptionData addTokens = createAddTokensMethodDescriptionData(getterAnnotation);
        MethodDescriptionData getTotal = createGetTotalMethodDescriptionData();
        MethodDescriptionData addToken = createAddTokenMethodDescriptionData(getterAnnotation);
        MethodDescriptionData testToken = createTestTokenMethodDescriptionData();
        MethodDescriptionData testNotToken = createTestNotTokenMethodDescriptionData();


        List<MethodDescriptionData> contractsMethods = ceService.getContractsMethods(byteCodeObjectDataList);
        Assert.assertEquals(findmethod(contractsMethods, "initialize"), initialize);
        Assert.assertEquals(findmethod(contractsMethods, "addTokens"), addTokens);
        Assert.assertEquals(findmethod(contractsMethods, "getTotal"), getTotal);
        Assert.assertEquals(findmethod(contractsMethods, "addToken"), addToken);
        Assert.assertEquals(findmethod(contractsMethods, "testToken"), testToken);
        Assert.assertEquals(findmethod(contractsMethods, "testNotToken"), testNotToken);


    }

    public MethodDescriptionData createInitializeMethodDescriptionData(AnnotationData getterAnnotation) {
        return new MethodDescriptionData("void", "initialize", new ArrayList<>(),
                Collections.singletonList(getterAnnotation));
    }

    public MethodDescriptionData createAddTokensMethodDescriptionData(AnnotationData getterAnnotation) {
        ArrayList<AnnotationData> addTokensAnnotationData = new ArrayList<>();
        addTokensAnnotationData.add(getterAnnotation);
        addTokensAnnotationData.add(new AnnotationData("Contract", new HashMap<String, String>() {{
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
        addTokenAnnotationData.add(new AnnotationData("Contract", new HashMap<String, String>() {{
            put("address", "test1");
            put("method", "getBalance");
        }}));

        return new MethodDescriptionData("void", "addToken",
            Collections.singletonList(new MethodArgumentData("int", "amount", Collections.singletonList(getterAnnotation))),
            addTokenAnnotationData);
    }

    public MethodDescriptionData createTestTokenMethodDescriptionData() {
        ArrayList<AnnotationData> testTokenAnnotationData = new ArrayList<>();
        testTokenAnnotationData.add(new AnnotationData("ContractAddress", new HashMap<String, String>() {{
            put("id", "0");
        }}));
        return new MethodDescriptionData("void", "testToken",
            Collections.singletonList(new MethodArgumentData("int", "amount", testTokenAnnotationData)), new ArrayList<>());
    }

    public MethodDescriptionData createTestNotTokenMethodDescriptionData() {
        ArrayList<AnnotationData> testNotTokenAnnotationData = new ArrayList<>();
        testNotTokenAnnotationData.add(new AnnotationData("ContractMethod", new HashMap<String, String>() {{
            put("id", "0");
        }}));

        return new MethodDescriptionData("void", "testNotToken",
            Collections.singletonList(new MethodArgumentData("int", "amount", testNotTokenAnnotationData)), new ArrayList<>());
    }

    private MethodDescriptionData findmethod(List<MethodDescriptionData> contractsMethods, String name) {
        for (MethodDescriptionData contractsMethod : contractsMethods) {
            if (contractsMethod.name.equals(name)) {
                return contractsMethod;
            }
        }
        return null;
    }


}
