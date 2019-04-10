package com.credits.service.contract;

import com.credits.general.classload.ByteCodeContractClassLoader;
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

import static com.credits.general.serialize.Serializer.deserialize;
import static com.credits.general.thrift.generated.Variant.v_boolean;
import static com.credits.general.thrift.generated.Variant.v_double;
import static com.credits.general.thrift.generated.Variant.v_double_box;
import static com.credits.general.thrift.generated.Variant.v_int;
import static com.credits.general.thrift.generated.Variant.v_int_box;
import static com.credits.general.thrift.generated.Variant.v_list;
import static com.credits.general.thrift.generated.Variant.v_long;
import static com.credits.general.thrift.generated.Variant.v_string;
import static com.credits.thrift.utils.ContractExecutorUtils.compileSmartContractByteCode;
import static com.credits.utils.ContractExecutorServiceUtils.castValues;
import static com.credits.utils.ContractExecutorServiceUtils.getMethodArgumentsValuesByNameAndParams;
import static java.util.Arrays.asList;

public class MethodParametersTest extends ServiceTest {

    private ByteCodeContractClassLoader byteCodeContractClassLoader;
    private Class<?> contractClass;
    private byte[] contractState;

    public MethodParametersTest() {
        super("/methodParametersTest/MethodParametersTest.java");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        byteCodeContractClassLoader = new ByteCodeContractClassLoader();
        contractClass = compileSmartContractByteCode(byteCodeObjectDataList, byteCodeContractClassLoader).get(0);
        contractState = deploySmartContract().newContractState;
    }


    @Test
    public void findVoidMethod() throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Variant[] voidParams = {};
        MethodData voidMethod = getMethodArgumentsValuesByNameAndParams(contractClass, "foo", voidParams, getClass().getClassLoader());

        Assert.assertEquals(voidMethod.method.toString(), "public static java.lang.Integer MethodParametersTest.foo()");

        Integer invokeResult = (Integer) voidMethod.method.invoke(
            deserialize(contractState, byteCodeContractClassLoader),
            castValues(voidMethod.argTypes, voidParams, getClass().getClassLoader()));
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void findSimpleMethod() throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        Variant[] simpleParams = {v_double(3f), v_double_box(4f), v_int(1), v_int_box(2), v_double(200d), v_double_box(220d)};
        MethodData simpleMethod = getMethodArgumentsValuesByNameAndParams(contractClass, "foo", simpleParams, getClass().getClassLoader());
        Assert.assertEquals(
            simpleMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double)");
        Object invoke = simpleMethod.method
            .invoke(
                deserialize(contractState, byteCodeContractClassLoader),
                castValues(simpleMethod.argTypes, simpleParams, getClass().getClassLoader()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }

    @Test
    public void findMethodWithArrayList() throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        Variant[] arrayList = {v_list(new ArrayList<>(asList(v_int(1), v_int(2), v_int(3))))};

        MethodData arrayListMethod = getMethodArgumentsValuesByNameAndParams(contractClass, "foo", arrayList, getClass().getClassLoader());
        Assert.assertEquals(
            arrayListMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(java.util.List)");
        Object invoke = arrayListMethod.method
            .invoke(
                deserialize(contractState, byteCodeContractClassLoader),
                castValues(arrayListMethod.argTypes, arrayList, getClass().getClassLoader()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void findAnotherMethodWithArrayList() throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        Variant[] arrayList = {v_list(new ArrayList<>(asList(v_int(1), v_int(2), v_int(3))))};
        MethodData arrayListMethod = getMethodArgumentsValuesByNameAndParams(contractClass, "fooInteger",
                                                                             arrayList, getClass().getClassLoader());
        Assert.assertEquals(
            arrayListMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.fooInteger(java.util.List)");
        Object invoke = arrayListMethod.method
            .invoke(
                deserialize(contractState, byteCodeContractClassLoader),
                castValues(arrayListMethod.argTypes, arrayList, getClass().getClassLoader()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }


    @Test
    public void findMethodWithSimpleParamsAndArrayList() throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        List<Variant> list = new ArrayList<>();
        list.add(v_string("string01"));
        list.add(v_string("string01"));
        list.add(v_string("string01"));

        Variant[] simpleParamsWithList =
            {v_double(3f), v_double(4f), v_int(1), v_int(2), v_double(200d), v_double(220d), v_list(list)};

        MethodData simpleAndArrayListMethod = getMethodArgumentsValuesByNameAndParams(contractClass, "foo", simpleParamsWithList, getClass().getClassLoader());

        Assert.assertEquals(
            simpleAndArrayListMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double,java.util.ArrayList)");
        Object invoke = simpleAndArrayListMethod.method
            .invoke(
                deserialize(contractState, byteCodeContractClassLoader),
                castValues(simpleAndArrayListMethod.argTypes, simpleParamsWithList, getClass().getClassLoader()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void moreVariousParameters()
        throws ContractExecutorException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
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

        MethodData moreVariousParametersMethod = getMethodArgumentsValuesByNameAndParams(contractClass, "foo", params, getClass().getClassLoader());
        Assert.assertEquals(
            moreVariousParametersMethod.method.toString(),
            "public java.lang.Integer MethodParametersTest.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List)");
        Object invoke = moreVariousParametersMethod.method
            .invoke(
                deserialize(contractState, byteCodeContractClassLoader),
                castValues(moreVariousParametersMethod.argTypes, params, getClass().getClassLoader()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);
    }

}
