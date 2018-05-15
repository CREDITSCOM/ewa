package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import com.credits.service.usercode.UserCodeStorageService;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.fail;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class ContractExecutorServiceSecurityTest extends ServiceTest {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Resource
    private ContractExecutorService service;
    @Resource
    private UserCodeStorageService storageService;

    @Parameter
    public String methodName;
    @Parameter(1)
    public String arg;
    @Parameter(2)
    public Boolean errorExpected;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"openSocket", "5555", true},
                {"setTotal", "1000", false},
                {"getTotal", null, false},
                {"createFile", null, true},
                {"killProcess", null, true},
                {"killThread", null, true},
                {"newThread", null, true},
        });
    }

    private final String address = "1a2b3cc4";

    @Before
    public void setUp() throws ContractExecutorException {
        clean(address);
        storeContractFile("ContractExecutorServiceSecurityTestCode.java", address);
        service.execute(address, "");
    }

    @Test
    public void test() throws ContractExecutorException {
        try {
            service.execute(address, methodName, arg != null ? new String[]{arg} : null);
        } catch (ContractExecutorException e) {
            e.printStackTrace();
            if (!errorExpected) fail();
        }
    }

    void storeContractFile(String name, String address) throws ContractExecutorException {
        File testFile = new File(name);
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("com/credits/service/usercode/" + name)) {
            FileUtils.copyToFile(stream, testFile);
            storageService.store(testFile, address);
            testFile.delete();
        } catch (ContractExecutorException | IOException e) {
            throw new ContractExecutorException(e.getMessage(), e);
        }
    }

}
