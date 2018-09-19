package com.credits.wallet.desktop.utils;

import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.AppState;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContractSaverTest {
    @Test
    public void saveSmartWithNotExistDirectoryTest() throws IOException, ClassNotFoundException {
        Map map = new HashMap();
        map.put("1",new SmartContractData(null, null, "AAA", null, "bad hash"));
        Path filePath = Paths.get(System.getProperty("user.dir") + File.separator + ContactSaver.fileDir);
        deleteDirectoryStream(filePath);
        ContactSaver.serialize(map);
        ContactSaver.deserialize();
        Assert.assertEquals(ContactSaver.deserialize(), map);
    }

    @Test
    public void saveSmartWithNotExistDirectoryWithSetAppstateTest() throws IOException, ClassNotFoundException {
        AppState.account = "222222";
        Map map = new HashMap();
        map.put("1",new SmartContractData(null, null, "AAA", null, "bad hash"));
        Path filePath = Paths.get(System.getProperty("user.dir") + File.separator + ContactSaver.fileDir);
        deleteDirectoryStream(filePath);
        ContactSaver.serialize(map);
        ContactSaver.deserialize();
        Assert.assertEquals(ContactSaver.deserialize(), map);
    }


    @Test
    public void saveSmartWithExistDirectoryTest() throws IOException, ClassNotFoundException {
        Map map = new HashMap();
        map.put("1",new SmartContractData(null, null, "AAA", null, "bad hash"));
        Path filePath = Paths.get(System.getProperty("user.dir") + File.separator + ContactSaver.fileDir);
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath);
        }
        ContactSaver.serialize(map);
        ContactSaver.deserialize();
        Assert.assertEquals(ContactSaver.deserialize(), map);
    }


    void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }
}
