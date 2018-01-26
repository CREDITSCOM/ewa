package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.service.usercode.UserCodeStorageService;
import com.credits.thrift.gen.api.Amount;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ContractExecutorServiceThriftIntegrationMockTest extends ServiceTest {

    @Resource
    private ContractExecutorService ceService;

    @Resource
    private UserCodeStorageService userCodeService;

    @MockBean
    private LevelDbInteractionService service;

    private final String address = "1a2b3c";

    @Before
    public void setUp() throws ContractExecutorException {
        Map<String, Amount> map = new HashMap<>();
        map.put("CS", new Amount(1, 25));
        when(service.getBalance(anyString())).thenReturn(map);

        String fileName = "MyTest.java";
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + fileName)) {
            MultipartFile file = new MockMultipartFile(fileName, fileName, null, stream);
            userCodeService.store(file, address);
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }

    @Test
    public void getBalanceTest() throws ContractExecutorException {
        Map<String, Amount> map = service.getBalance("");
        Assert.assertNotNull(map);
        Assert.assertNotEquals(0, map.size());
        Assert.assertTrue(map.containsKey("CS"));
        Assert.assertEquals(1, map.get("CS").getIntegral());
        Assert.assertEquals(25, map.get("CS").getFraction());
    }

    @Test
    public void dependencyInjectorTest() throws ContractExecutorException {
        ceService.execute(address, "foo1", null);
    }
}
