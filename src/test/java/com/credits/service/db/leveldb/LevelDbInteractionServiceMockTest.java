package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.contract.ContractExecutorService;
import com.credits.vo.usercode.Transaction;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class LevelDbInteractionServiceMockTest extends ServiceTest {

    @Resource
    private ContractExecutorService ceService;

    @MockBean
    private LevelDbInteractionService service;

    private final String address = "1a2b";

    @Before
    public void SetUp() throws ContractExecutorException {
        when(service.get(anyString(), anyInt())).thenReturn(new Transaction[] {new Transaction("123", 1, '+'),
            new Transaction("123", 2, '+'), new Transaction("456", 3, '-')});

        final String destFolder = System.getProperty("user.dir") + File.separator + "credits";
        URL resource = getClass().getClassLoader().getResource("com/credits/service/contract/UserCodeTest.class");
        Assert.assertNotNull(resource);

        File source = new File(resource.getFile());

        String destFilePath = destFolder + File.separator + address + File.separator + source.getName();
        File dest = new File(destFilePath);
        dest.getParentFile().mkdirs();

        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }

    @Test
    public void getTest() throws ContractExecutorException {
        Transaction[] transactions = service.get("", 1);
        Assert.assertNotNull(transactions);
        Assert.assertNotEquals(0, transactions.length);
        Assert.assertEquals("456", transactions[2].getId());
        Assert.assertEquals(3, transactions[2].getValue());
        Assert.assertEquals('-', transactions[2].getOperation());
    }

    @Test
    public void primitiveExecutionTest() throws ContractExecutorException {
        String[] params = {"\"test string\"", "200", "3f"};
        ceService.execute(address, "foo", params);
    }
}
