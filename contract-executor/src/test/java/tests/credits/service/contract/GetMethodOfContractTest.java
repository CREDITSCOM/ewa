package tests.credits.service.contract;

import com.credits.general.pojo.AnnotationData;
import com.credits.general.pojo.MethodArgumentData;
import com.credits.general.pojo.MethodDescriptionData;
import org.junit.Test;
import tests.credits.service.ServiceTest;

import java.util.ArrayList;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;

public class GetMethodOfContractTest extends ServiceTest {

    public GetMethodOfContractTest() {
        super("/getMethodsTest/MySmartContract.java");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void get_methods_of_contract() {

        MethodDescriptionData[] expectedMethods = new MethodDescriptionData[] {
            new MethodDescriptionData("void", "initialize", new ArrayList<>(), new ArrayList<>()),

            new MethodDescriptionData(
                "void",
                "addTokens",
                singletonList(new MethodArgumentData("java.lang.Integer", "amount", EMPTY_LIST)),
                new ArrayList<>()),

            new MethodDescriptionData(
                "boolean",
                "payable",
                asList(
                    new MethodArgumentData("java.lang.String", "amount", EMPTY_LIST),
                    new MethodArgumentData("java.lang.String", "currency", EMPTY_LIST)),
                EMPTY_LIST),

            new MethodDescriptionData(
                "int",
                "externalCall",
                asList(
                    new MethodArgumentData(
                        "java.lang.String",
                        "address",
                        singletonList(
                            new AnnotationData(
                                "com.credits.scapi.annotations.ContractAddress",
                                new HashMap() {{
                                    put("id", "0");
                                }}))),
                    new MethodArgumentData(
                        "java.lang.String",
                        "method",
                        singletonList(
                            new AnnotationData(
                                "com.credits.scapi.annotations.ContractMethod",
                                new HashMap() {{
                                    put("id", "0");
                                }})))),
                asList(
                    new AnnotationData(
                        new AnnotationData(
                            "com.credits.scapi.annotations.UsingContract",
                            new HashMap() {{
                                put("address", "address");
                                put("method", "method");
                            }})),
                    new AnnotationData(
                        "com.credits.scapi.annotations.UsingContract",
                        new HashMap() {{
                            put("address", "address");
                            put("method", "method");
                        }})))
        };


        MethodDescriptionData[] contractsMethods = ceService.getContractsMethods(byteCodeObjectDataList).toArray(new MethodDescriptionData[1]);

        assertThat(contractsMethods, arrayContainingInAnyOrder(expectedMethods));
    }
}
