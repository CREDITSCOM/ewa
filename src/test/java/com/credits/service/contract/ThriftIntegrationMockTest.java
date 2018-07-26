package com.credits.service.contract;

import com.credits.service.ServiceTest;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static com.credits.TestUtils.SimpleInMemoryCompiler.compile;
import static java.io.File.separator;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

public class ThriftIntegrationMockTest extends ServiceTest {
    @Resource
    private LevelDbInteractionService dbservice;

    private byte[] contractBytecode;
    private byte[] contractState;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(mockClient.getBalance(anyString(), anyByte())).thenReturn(new BigDecimal(555));

        Field client = dbservice.getClass().getDeclaredField("client");
        client.setAccessible(true);
        client.set(dbservice, mockClient);

        Class<?> contract = Class.forName("SmartContract");
        Field interactionService = contract.getDeclaredField("service");
        interactionService.setAccessible(true);
        interactionService.set(null, dbservice);

        String sourceCode = readSourceCode("/thriftIntegrationTest/Contract.java");
        contractBytecode = compile(sourceCode, "Contract", "TKN");

        contractState = ceService.execute(address, contractBytecode, null, null, null).getContractState();
    }

    @After
    public void tearDown() {
        String dir = System.getProperty("user.dir") + separator + "credits";
        FileSystemUtils.deleteRecursively(new File(dir));
    }

    @Test
    public void execute_contract_using_bytecode_getBalance() throws Exception {
        String balance = (String) ceService.execute(address, contractBytecode,
            contractState, "balanceGet", new String[0]).getVariant().getFieldValue();
        Assert.assertEquals("555", balance);
    }
}
