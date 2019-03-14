package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import com.credits.service.node.api.NodeApiInteractionService;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static java.io.File.separator;
import static org.junit.Assert.assertEquals;

public class ThriftIntegrationMockTest extends ServiceTest {

    @Inject
    NodeApiInteractionService dbservice;

    private byte[] contractState;

    public ThriftIntegrationMockTest() {
        super("/thriftIntegrationTest/MySmartContract.java");
    }

    @Before
    public void setUp() throws Exception {
        Field client = dbservice.getClass().getDeclaredField("service");
        client.setAccessible(true);
        client.set(dbservice, mockNodeApiExecService);

        Class<?> contract = Class.forName("SmartContract");
        Field interactionService = contract.getDeclaredField("service");
        interactionService.setAccessible(true);
        interactionService.set(null, dbservice);

        contractState = deploySmartContract().newContractState;
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    @Test
    @Ignore("need resolve file permission for this test")
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        //fixme
//        when(mockNodeApiExecService.getBalance(any())).thenReturn(new BigDecimal(555));
//        String balance = executeSmartContract("balanceGet", contractState).executeResults.get(0).result.getV_string();
//        assertEquals("555", balance);
    }

    @Test
    @Ignore("need resolve file permission for this test")
    public void execute_contract_method_with_variant_parameters() throws ContractExecutorException {
        Integer newValue =
            executeSmartContract(
                "addValue",
                new Variant[][] {{new Variant(Variant._Fields.V_INT_BOX, 112233)}},
                contractState).executeResults.get(0).result.getV_int_box();
        assertEquals(112233, newValue.intValue());
    }
}
