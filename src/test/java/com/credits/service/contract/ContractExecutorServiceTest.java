package com.credits.service.contract;

import com.credits.exception.ContractExecutorException;
import com.credits.service.ServiceTest;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Map;

public class ContractExecutorServiceTest extends ServiceTest {

    @Resource
    private ContractExecutorService service;

    private final String address = "1a2b";

    private File file;


    @Before
    public void setUp() throws ContractExecutorException {
        file = new File(System.getProperty("user.dir") + File.separator + "credits" +
            File.separator + address + File.separator + "UserCodeTest.out");
        if (file.exists()) {
            file.delete();
        }

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
    public void primitiveExecutionTest() throws ContractExecutorException {
        String[] params = {"\"test string\"", "200", "3f"};
        service.execute(address, "foo", params);
    }

    @Test
    public void objectExecutionTest() throws ContractExecutorException {
        String[] params = {"\"test string\"", "200d", "3"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayExecutionTest() throws ContractExecutorException {
        String[] params = { "{\"test1\", \"test2\", \"test3\"}" };
        service.execute(address, "main", params);

        params = new String[]{"{1, 2, 3}"};
        service.execute(address, "main", params);

        params = new String[]{"{1d, 2d, 3d}"};
        service.execute(address, "main", params);
    }

    @Test
    public void arrayBooleanTest() throws ContractExecutorException {
        String[] params = {"{false, true}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayIntTest() throws ContractExecutorException {
        String[] params = {"{1, 2}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayShortTest() throws ContractExecutorException {
        String[] params = {"{(short)1, (short)2}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayLongTest() throws ContractExecutorException {
        String[] params = {"{1l, 2l}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void arrayFloatTest() throws ContractExecutorException {
        String[] params = {"{1f, .2f}"};
        service.execute(address, "foo", params);
    }

    @Test
    public void globalVarInstanceTest() throws ContractExecutorException {
        service.execute(address, "globalVarInstance", new String[0]);
        service.execute(address, "globalVarInstance", new String[0]);

        Map<String, Object> deserFields = null;
        try (ObjectInputStream ous = new ObjectInputStream(new FileInputStream(file))){
            deserFields = (Map<String, Object>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(deserFields);
        Assert.assertEquals(3, deserFields.get("intVar"));
    }

    @Test
    public void globalVarStaticTest() throws ContractExecutorException {
        service.execute(address, "globalVarStatic", new String[0]);
        service.execute(address, "globalVarStatic", new String[0]);

        Map<String, Object> deserFields = null;
        try (ObjectInputStream ous = new ObjectInputStream(new FileInputStream(file))){
            deserFields = (Map<String, Object>) ous.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(deserFields);
        Assert.assertEquals(4, deserFields.get("statIntVar"));
    }
}
