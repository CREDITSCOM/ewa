package com.credits.service.db.leveldb;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.contract.ContractExecutorService;
import com.credits.service.usercode.UserCodeStorageService;
import com.credits.vo.usercode.Transaction;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class LevelDbInteractionServiceMockTest extends ServiceTest {

    @Resource
    private ContractExecutorService ceService;

    @Resource
    private UserCodeStorageService userCodeService;

    @MockBean
    private LevelDbInteractionService service;

    private final String address = "1a2b";

    @Before
    public void setUp() throws ContractExecutorException {
        when(service.get(anyString(), anyInt())).thenReturn(new Transaction[] {new Transaction("123", 1, '+'),
            new Transaction("123", 2, '+'), new Transaction("456", 3, '-')});

        String fileName = "MyTest.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            userCodeService.store(file, address);
        } catch (ContractExecutorException | IOException e) {
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
    public void dependencyInjectorTest() throws ContractExecutorException {
        String[] params = null;
        ceService.execute(address, "work", params);
    }
}
