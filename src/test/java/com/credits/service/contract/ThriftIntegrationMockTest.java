package com.credits.service.contract;

import com.credits.service.ServiceTest;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.thrift.generated.Variant;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static java.io.File.separator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ThriftIntegrationMockTest extends ServiceTest {
    @Inject
    LevelDbInteractionService dbservice;

    private byte[] contractBytecode;
    private byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        testComponent.inject(this);
        when(mockLevelDbService.getBalance(any())).thenReturn(new BigDecimal(555));

        Field client = dbservice.getClass().getDeclaredField("service");
        client.setAccessible(true);
        client.set(dbservice, mockLevelDbService);

        Class<?> contract = Class.forName("SmartContract");
        Field interactionService = contract.getDeclaredField("service");
        interactionService.setAccessible(true);
        interactionService.set(null, dbservice);

        String sourceCode = readSourceCode("/thriftIntegrationTest/Contract.java");
        contractBytecode = compile(sourceCode, "Contract", "TKN");

        contractState = ceService.execute(address, contractBytecode, null, null, null).getContractState();
    }

    @After
    public void tearDown() throws IOException {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileUtils.deleteDirectory(new File(dir));
    }

    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        String balance = (String) ceService.execute(address, contractBytecode,
            contractState, "balanceGet", new Variant[0]).getVariant().getFieldValue();
        Assert.assertEquals("555", balance);
    }
}
