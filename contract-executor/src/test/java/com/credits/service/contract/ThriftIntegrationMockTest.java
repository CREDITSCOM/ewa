package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.general.thrift.generated.Variant;
import com.credits.service.ServiceTest;
import com.credits.service.node.api.NodeApiInteractionService;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static java.io.File.separator;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ThriftIntegrationMockTest extends ServiceTest {

    @Inject
    NodeApiInteractionService dbservice;

    private byte[] contractBytecode;
    private byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        testComponent.inject(this);

        Field client = dbservice.getClass().getDeclaredField("service");
        client.setAccessible(true);
        client.set(dbservice, mockNodeApiService);

        Class<?> contract = Class.forName("SmartContract");
        Field interactionService = contract.getDeclaredField("service");
        interactionService.setAccessible(true);
        interactionService.set(null, dbservice);

        String sourceCode = readSourceCode("/thriftIntegrationTest/Contract.java");
        contractBytecode = compile(sourceCode, "Contract", "TKN");

        contractState = ceService.execute(address, contractBytecode, null, null, null,500L).getContractState();
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        when(mockNodeApiService.getBalance(any())).thenReturn(new BigDecimal(555));
        String balance = (String) ceService.execute(address, contractBytecode,
            contractState, "balanceGet", new Variant[][] {{}},500L).getVariantsList().get(0).getFieldValue();
        assertEquals("555", balance);
    }

    @Test
    public void execute_contract_method_with_variant_parameters() throws ContractExecutorException {
        Integer newValue =
        ceService.execute(address, contractBytecode,
            contractState, "addValue", new Variant[][]{{new Variant(Variant._Fields.V_INT_BOX, 112233)}},500L).getVariantsList().get(0).getV_int_box();
        assertEquals(112233, newValue.intValue());

    }
}
