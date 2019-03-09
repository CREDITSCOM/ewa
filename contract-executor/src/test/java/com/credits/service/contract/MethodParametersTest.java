package com.credits.service.contract;

import com.credits.classload.BytecodeContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.pojo.MethodData;
import com.credits.service.ServiceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.credits.general.thrift.generated.Variant.v_boolean;
import static com.credits.general.thrift.generated.Variant.v_double;
import static com.credits.general.thrift.generated.Variant.v_double_box;
import static com.credits.general.thrift.generated.Variant.v_int;
import static com.credits.general.thrift.generated.Variant.v_int_box;
import static com.credits.general.thrift.generated.Variant.v_list;
import static com.credits.general.thrift.generated.Variant.v_long;
import static com.credits.general.thrift.generated.Variant.v_string;
import static com.credits.serialize.Serializer.deserialize;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.utils.ContractExecutorServiceUtils.castValues;
import static java.util.Arrays.asList;

public class MethodParametersTest extends ServiceTest {

    private BytecodeContractClassLoader classLoader;
    private Class<?> contractClass;
    private byte[] contractState;

    public MethodParametersTest() {
        super("/methodParametersTest/MethodParametersTest.java");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        classLoader = new BytecodeContractClassLoader();
        contractClass = compileSmartContractByteCode(byteCodeObjectDataList, classLoader);
        contractState = deploySmartContract().newContractState;
    }


    @Test
    public void findVoidMethod() throws InvocationTargetException, IllegalAccessException {
        Variant[] voidParams = {};
        MethodData voidMethod = ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", voidParams);

        Assert.assertEquals(voidMethod.method.toString(), "public static java.lang.Integer MethodParametersTest.foo()");

        Integer invokeResult = (Integer) voidMethod.method.invoke(
            deserialize(contractState, classLoader),
            castValues(voidMethod.argTypes, voidMethod.argValues));
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void findSimpleMethod() throws InvocationTargetException, IllegalAccessException {

        Variant[] simpleParams = {v_double(3f), v_double_box(4f), v_int(1), v_int_box(2), v_double(200d), v_double_box(220d)};
        MethodData simpleMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", simpleParams);
        Assert.assertEquals(
            simpleMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double)");
        Object invoke = simpleMethod.method
            .invoke(
                deserialize(contractState, classLoader),
                castValues(simpleMethod.argTypes, simpleMethod.argValues));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }

    @Test
    public void findMethodWithArrayList() throws InvocationTargetException, IllegalAccessException {

        Variant[] arrayList = {v_list(new ArrayList<>(asList(v_int(1), v_int(2), v_int(3))))};

        MethodData arrayListMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", arrayList);
        Assert.assertEquals(
            arrayListMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(java.util.List)");
        Object invoke = arrayListMethod.method
            .invoke(
                deserialize(contractState, classLoader),
                castValues(arrayListMethod.argTypes, arrayListMethod.argValues));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void findAnotherMethodWithArrayList() throws InvocationTargetException, IllegalAccessException {

        Variant[] arrayList = {v_list(new ArrayList<>(asList(v_int(1), v_int(2), v_int(3))))};
        MethodData arrayListMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "fooInteger",
                                                                                arrayList);
        Assert.assertEquals(
            arrayListMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.fooInteger(java.util.List)");
        Object invoke = arrayListMethod.method
            .invoke(
                deserialize(contractState, classLoader),
                castValues(arrayListMethod.argTypes, arrayListMethod.argValues));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }


    @Test
    public void findMethodWithSimpleParamsAndArrayList() throws InvocationTargetException, IllegalAccessException {

        List<Variant> list = new ArrayList<>();
        list.add(v_string("string01"));
        list.add(v_string("string01"));
        list.add(v_string("string01"));

        Variant[] simpleParamsWithList =
            {v_double(3f), v_double(4f), v_int(1), v_int(2), v_double(200d), v_double(220d), v_list(list)};

        MethodData simpleAndArrayListMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo",
                                                                                simpleParamsWithList);

        Assert.assertEquals(
            simpleAndArrayListMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double,java.util.ArrayList)");
        Object invoke = simpleAndArrayListMethod.method
            .invoke(
                deserialize(contractState, classLoader),
                castValues(simpleAndArrayListMethod.argTypes, simpleAndArrayListMethod.argValues));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void moreVariousParameters()
        throws ContractExecutorException, InvocationTargetException, IllegalAccessException {
        List<Variant> list = new LinkedList<>();
        list.add(v_string("string01"));
        list.add(v_string("string01"));
        list.add(v_string("string01"));
        Variant[] params =
            {
                v_double(3f), v_double(4f), v_int(1), v_int(2), v_double(200d), v_double(220d), v_list(list),
                v_list(new ArrayList<>(asList(v_int(1), v_int(2), v_int(3), v_int(4)))),
                v_list(new ArrayList<>(asList(v_int(5), v_int(6), v_int(7), v_int(8)))),
                v_list(new ArrayList<>(asList(v_double(1d), v_double(2d), v_double(3d)))),
                v_list(new ArrayList<>(asList(v_double(4d), v_double(5d), v_double(6d)))),
                v_list(new ArrayList<>(asList(v_boolean(true), v_boolean(true), v_boolean(false)))),
                v_list(new ArrayList<>(asList(v_boolean(true), v_boolean(true), v_boolean(false)))),
                v_list(new ArrayList<>(asList(v_int((short) 1), v_int((short) 2)))),
                v_list(new ArrayList<>(asList(v_long(1L), v_long(2L), v_long(3L)))),
                v_list(new ArrayList<>(asList(v_long(4L), v_long(5L), v_long(6L)))),
                v_list(new ArrayList<>(asList(v_double(1f), v_double(.2f)))),
                v_list(new ArrayList<>(asList(v_double(3f), v_double(.4f))))
            };

        MethodData moreVariousParametersMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", params);
        Assert.assertEquals(
            moreVariousParametersMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List)");
        Object invoke = moreVariousParametersMethod.method
            .invoke(
                deserialize(contractState, classLoader),
                castValues(moreVariousParametersMethod.argTypes, moreVariousParametersMethod.argValues));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }

}
