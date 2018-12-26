package com.credits.service.contract;

import com.credits.classload.ByteArrayContractClassLoader;
import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.pojo.MethodArgumentsValuesData;
import com.credits.service.ServiceTest;
import com.credits.utils.ContractExecutorServiceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.credits.general.thrift.generated.Variant.v_bool;
import static com.credits.general.thrift.generated.Variant.v_double;
import static com.credits.general.thrift.generated.Variant.v_i16;
import static com.credits.general.thrift.generated.Variant.v_i32;
import static com.credits.general.thrift.generated.Variant.v_i64;
import static com.credits.general.thrift.generated.Variant.v_list;
import static com.credits.general.thrift.generated.Variant.v_string;
import static com.credits.serialize.Serializer.deserialize;
import static java.util.Arrays.asList;

public class MethodParametersTest extends ServiceTest {

    private ByteArrayContractClassLoader classLoader;
    private Class<?> contractClass;
    private byte[] contractBytecode;
    private byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        contractBytecode = compileSourceCode("/methodParametersTest/Contract.java");
        classLoader = new ByteArrayContractClassLoader();
        contractClass = classLoader.buildClass(contractBytecode);
        contractState = ceService.execute(address, contractBytecode, null, null, null, 500L).getContractState();
    }


    @Test
    public void findVoidMethod() throws InvocationTargetException, IllegalAccessException {
        Variant[] voidParams = {};
        MethodArgumentsValuesData voidMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", voidParams);
        Assert.assertEquals(voidMethod.getMethod().toString(), "public static java.lang.Integer Contract.foo()");
        Object invoke =
            voidMethod.getMethod().invoke(deserialize(contractState, classLoader), ContractExecutorServiceUtils.castValues(voidMethod.getArgTypes(),voidMethod.getArgValues()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);
    }

    @Test
    public void findSimpleMethod() throws InvocationTargetException, IllegalAccessException {

        Variant[] simpleParams = {v_double(3f), v_double(4f), v_i32(1), v_i32(2), v_double(200d), v_double(220d)};
        MethodArgumentsValuesData simpleMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", simpleParams);
        Assert.assertEquals(simpleMethod.getMethod().toString(),
            "public java.lang.Integer Contract.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double)");
        Object invoke = simpleMethod.getMethod()
            .invoke(deserialize(contractState, classLoader), ContractExecutorServiceUtils.castValues(simpleMethod.getArgTypes(),simpleMethod.getArgValues()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }

    @Test
    public void findMethodWithArrayList() throws InvocationTargetException, IllegalAccessException {

        Variant[] arrayList = {v_list(new ArrayList<>(asList(v_i32(1), v_i32(2), v_i32(3))))};


        MethodArgumentsValuesData arrayListMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", arrayList);
        Assert.assertEquals(arrayListMethod.getMethod().toString(),
            "public java.lang.Integer Contract.foo(java.util.List)");
        Object invoke = arrayListMethod.getMethod()
            .invoke(deserialize(contractState, classLoader), ContractExecutorServiceUtils.castValues(arrayListMethod.getArgTypes(),arrayListMethod.getArgValues()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }

    @Test
    public void findAnotherMethodWithArrayList() throws InvocationTargetException, IllegalAccessException {

        Variant[] arrayList = {v_list(new ArrayList<>(asList(v_i32(1), v_i32(2), v_i32(3))))};
        MethodArgumentsValuesData arrayListMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "fooInteger",
                arrayList);
        Assert.assertEquals(arrayListMethod.getMethod().toString(),
            "public java.lang.Integer Contract.fooInteger(java.util.List)");
        Object invoke = arrayListMethod.getMethod()
            .invoke(deserialize(contractState, classLoader), ContractExecutorServiceUtils.castValues(arrayListMethod.getArgTypes(),arrayListMethod.getArgValues()));
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
            {v_double(3f), v_double(4f), v_i32(1), v_i32(2), v_double(200d), v_double(220d), v_list(list)};

        MethodArgumentsValuesData simpleAndArrayListMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo",
                simpleParamsWithList);

        Assert.assertEquals(simpleAndArrayListMethod.getMethod().toString(),
            "public java.lang.Integer Contract.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double,java.util.ArrayList)");
        Object invoke = simpleAndArrayListMethod.getMethod()
            .invoke(deserialize(contractState, classLoader), ContractExecutorServiceUtils.castValues(simpleAndArrayListMethod.getArgTypes(),simpleAndArrayListMethod.getArgValues()));
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
            {v_double(3f), v_double(4f), v_i32(1), v_i32(2), v_double(200d), v_double(220d), v_list(list),
                v_list(new ArrayList<>(asList(v_i32(1), v_i32(2), v_i32(3), v_i32(4)))),
                v_list(new ArrayList<>(asList(v_i32(5), v_i32(6), v_i32(7), v_i32(8)))),
                v_list(new ArrayList<>(asList(v_double(1d), v_double(2d), v_double(3d)))),
                v_list(new ArrayList<>(asList(v_double(4d), v_double(5d), v_double(6d)))),
                v_list(new ArrayList<>(asList(v_bool(true), v_bool(true), v_bool(false)))),
                v_list(new ArrayList<>(asList(v_bool(true), v_bool(true), v_bool(false)))),
                v_list(new ArrayList<>(asList(v_i16((short) 1), v_i16((short) 2)))),
                v_list(new ArrayList<>(asList(v_i64(1L), v_i64(2L), v_i64(3L)))),
                v_list(new ArrayList<>(asList(v_i64(4L), v_i64(5L), v_i64(6L)))),
                v_list(new ArrayList<>(asList(v_double(1f), v_double(.2f)))),
                v_list(new ArrayList<>(asList(v_double(3f), v_double(.4f))))};

        MethodArgumentsValuesData moreVariousParametersMethod =
            ContractExecutorServiceImpl.getMethodArgumentsValuesByNameAndParams(contractClass, "foo", params);
        Assert.assertEquals(moreVariousParametersMethod.getMethod().toString(),
            "public java.lang.Integer Contract.foo(double,java.lang.Double,int,java.lang.Integer,double,java.lang.Double,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List,java.util.List)");
        Object invoke = moreVariousParametersMethod.getMethod()
            .invoke(deserialize(contractState, classLoader), ContractExecutorServiceUtils.castValues(moreVariousParametersMethod.getArgTypes(), moreVariousParametersMethod.getArgValues()));
        Integer invokeResult = (Integer) invoke;
        Assert.assertEquals(new Integer(1), invokeResult);

    }

}
