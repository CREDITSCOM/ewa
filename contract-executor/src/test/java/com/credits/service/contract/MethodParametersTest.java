package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import com.credits.thrift.utils.ContractUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
import static java.util.Arrays.asList;

public class MethodParametersTest extends ServiceTest {
    private byte[] contractBytecode;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        contractBytecode = compileSourceCode("/methodParametersTest/Contract.java");
    }
    @Test
    public void primitiveExecutionTest() throws ContractExecutorException {
        Variant[][] params = {{v_string("test string"), v_string("200"), v_string("3f")}};
        ceService.execute(address, contractBytecode, null, "foo", params,300);
    }

    @Test
    public void objectExecutionTest() throws ContractExecutorException {
        Variant[][] params = {{v_string("test string"), v_string("200d"), v_string("3")}};
        ceService.execute(address, contractBytecode,null, "foo", params,300);
    }

    @Test
    public void arrayExecutionTest() throws ContractExecutorException {

        Variant[][] params = {{v_list(asList(v_string("test1"), v_string("test2"), v_string("test3")))}};
        ceService.execute(address, contractBytecode,null, "main", params,300);

        params = new Variant[][] {{v_list(asList(v_i32(1), v_i32(2), v_i32(3)))}};
        ceService.execute(address, contractBytecode,null, "main", params,300);

        params = new Variant[][] {{v_list(asList(v_double(1d), v_double(2d), v_double(3d)))}};
        ceService.execute(address, contractBytecode,null, "main", params,300);
    }

    @Test
    public void executionTest() throws ContractExecutorException {
        Variant[][] params = {{
                ContractUtils.mapObjectToVariant("test string"),
                ContractUtils.mapObjectToVariant(200),
                ContractUtils.mapObjectToVariant(3D),
        }};
        ceService.execute(address, contractBytecode,null, "foo", params,500L);
    }

    @Test
    public void collectionExecutionTest() throws ContractExecutorException {
        Variant[] temp = new Variant[0];
        Variant[][] params = {{
                ContractUtils.mapObjectToVariant("test1"),
                ContractUtils.mapObjectToVariant("test2"),
                ContractUtils.mapObjectToVariant("test3")
        }};
        ceService.execute(address, contractBytecode,null, "main", params,500L);

        List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(2);
        intList.add(3);

        ContractUtils.mapObjectToVariant(intList).getV_list().toArray(temp);
        params = new Variant[][] {temp};


        ceService.execute(address, contractBytecode,null, "main", params,500L);

        List<Double> doubleList = new ArrayList<>();
        doubleList.add(1D);
        doubleList.add(2D);
        doubleList.add(3D);

        ContractUtils.mapObjectToVariant(doubleList).getV_list().toArray(temp);
        params = new Variant[][] {temp};

        ceService.execute(address, contractBytecode,null, "main", params,500L);
    }

    @Test
    public void arrayBooleanTest() throws ContractExecutorException {
        Variant[] temp = new Variant[0];
        Variant[][] params = {{}};
        List<Boolean> boolList = new ArrayList<>();
        boolList.add(true);
        boolList.add(false);
        ContractUtils.mapObjectToVariant(boolList).getV_list().toArray(temp);
        params = new Variant[][] {temp};
        ceService.execute(address, contractBytecode,null, "foo", params,500L);
    }

    @Test
    public void arrayIntTest() throws ContractExecutorException {
        Variant[] temp = new Variant[0];
        Variant[][] params = {{}};
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(temp);
        params = new Variant[][] {temp};
        ceService.execute(address, contractBytecode,null, "foo", params,500L);
    }

    @Test
    public void arrayShortTest() throws ContractExecutorException {
        Variant[] temp = new Variant[0];
        Variant[][] params = {{}};
        List<Short> list = new ArrayList<>();
        list.add((short)1);
        list.add((short)2);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(temp);
        params = new Variant[][] {temp};
        ceService.execute(address, contractBytecode,null, "foo", params,500L);
    }

    @Test
    public void arrayLongTest() throws ContractExecutorException {
        Variant[] temp = new Variant[0];
        Variant[][] params = {{}};
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(temp);
        params = new Variant[][] {temp};
        ceService.execute(address, contractBytecode,null, "foo", params,500L);
    }

    @Test
    @Ignore
    public void arrayFloatTest() throws ContractExecutorException {
        Variant[] temp = new Variant[0];
        Variant[][] params = {{}};
        List<Float> list = new ArrayList<>();
        list.add(1f);
        list.add(.2f);
        ContractUtils.mapObjectToVariant(list).getV_list().toArray(temp);
        params = new Variant[][] {temp};
        ceService.execute(address, contractBytecode,null, "foo", params,500L);
    }

    @Ignore
    @Test
    public void moreVariousParameters() throws ContractExecutorException {
        List<Variant> list = new LinkedList<>();
        list.add(v_string("string01"));
        list.add(v_string("string01"));
        list.add(v_string("string01"));
        Variant[] params =
            {v_double(3f), v_double(4f), v_i32(1), v_i32(2), v_double(200d), v_double(220d),
                v_list(list),
                v_list(asList(v_i32(1), v_i32(2), v_i32(3), v_i32(4))),
                v_list(asList(v_i32(5), v_i32(6), v_i32(7), v_i32(8))),
                v_list(asList(v_double(1d), v_double(2d), v_double(3d))),
                v_list(asList(v_double(4d), v_double(5d), v_double(6d))),
                v_list(asList(v_bool(true), v_bool(true), v_bool(false))),
                v_list(asList(v_bool(true), v_bool(true), v_bool(false))),
                v_list(asList(v_i16((short) 1), v_i16((short) 2))),
                v_list(asList(v_i64(1L), v_i64(2L), v_i64(3L))),
                v_list(asList(v_i64(4L), v_i64(5L), v_i64(6L))),
                v_list(asList(v_double(1f), v_double(.2f))), v_list(asList(v_double(3f), v_double(.4f)))};
//        List list = new ArrayList<>(asList(stringParams));
//        Variant[] params = {};
//        ContractUtils.mapObjectToVariant(list).getV_list().toArray(params);
        byte[] state = ceService.execute(address, contractBytecode, null, null, null, 500L).getContractState();
        ceService.execute(address, contractBytecode, state, "foo", new Variant[][]{params}, 500L);
    }
}