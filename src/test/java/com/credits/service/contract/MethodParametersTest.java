package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.thrift.generated.Variant;
import com.credits.thrift.utils.ContractUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodParametersTest extends ServiceTest {
    private byte[] contractBytecode;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        contractBytecode = compileSourceCode("/methodParametersTest/Contract.java");
    }

    @Test
    public void executionTest() throws ContractExecutorException {
        Variant[] params = {
                ContractUtils.mapObjectToVariant("test string"),
                ContractUtils.mapObjectToVariant(200),
                ContractUtils.mapObjectToVariant(3D),
        };
        ceService.execute(address, contractBytecode,null, "foo", params);
    }

    @Test
    public void collectionExecutionTest() throws ContractExecutorException {
        Variant[] params = {
                ContractUtils.mapObjectToVariant("test1"),
                ContractUtils.mapObjectToVariant("test2"),
                ContractUtils.mapObjectToVariant("test3")
        };
        ceService.execute(address, contractBytecode,null, "main", params);

        List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);

        ContractUtils.mapObjectToVariant(intList).getV_list().toArray(params);

        ceService.execute(address, contractBytecode,null, "main", params);

        List<Double> doubleList = new ArrayList<>();
        doubleList.add(1D);
        doubleList.add(2D);
        doubleList.add(3D);

        ContractUtils.mapObjectToVariant(doubleList).getV_list().toArray(params);

        ceService.execute(address, contractBytecode,null, "main", params);
    }

    @Test
    public void arrayBooleanTest() throws ContractExecutorException {
        Variant[] params = {};
        List<Boolean> boolList = new ArrayList<>();
        boolList.add(true);
        boolList.add(false);
        ContractUtils.mapObjectToVariant(boolList).getV_list().toArray(params);
        ceService.execute(address, contractBytecode,null, "foo", params);
    }

    @Test
    public void arrayIntTest() throws ContractExecutorException {
        Variant[] params = {};
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(params);
        ceService.execute(address, contractBytecode,null, "foo", params);
    }

    @Test
    public void arrayShortTest() throws ContractExecutorException {
        Variant[] params = {};
        List<Short> list = new ArrayList<>();
        list.add((short)1);
        list.add((short)2);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(params);
        ceService.execute(address, contractBytecode,null, "foo", params);
    }

    @Test
    public void arrayLongTest() throws ContractExecutorException {
        Variant[] params = {};
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(params);
        ceService.execute(address, contractBytecode,null, "foo", params);
    }

    @Test
    @Ignore
    public void arrayFloatTest() throws ContractExecutorException {
        Variant[] params = {};
        List<Float> list = new ArrayList<>();
        list.add(1f);
        list.add(.2f);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(params);
        ceService.execute(address, contractBytecode,null, "foo", params);
    }

    @Ignore
    @Test
    public void moreVariousParameters() throws ContractExecutorException {

        String[] stringParams = {
            "3f", "4f", "1", "2", "200d", "220d",
            "{\"string01\",\"string02\",\"string03\"},{1,2,3,4},{5,6,7,8},{1d,2d,3d},{4d,5d,6d},{true,true,false},{true,true,false},{(short)1,(short)2},{1l,2l,3l},{4l,5l,6l},{1f,.2f},{3f,.4f}"
        };
        List list = new ArrayList<>((Arrays.asList(stringParams)));
        Variant[] params = {};
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(params);
        ceService.execute(address,contractBytecode,null,"foo",params);
    }
}