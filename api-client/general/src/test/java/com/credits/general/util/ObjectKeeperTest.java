package com.credits.general.util;

import com.credits.general.pojo.SmartContractData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
public class ObjectKeeperTest {

    ConcurrentHashMap<String, SmartContractData> someData = new ConcurrentHashMap<>();
    ObjectKeeper<ConcurrentHashMap<String,SmartContractData>> objectKeeper;
    String account = "G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ";

    @Before
    public void setUp() throws IOException {
        objectKeeper = new ObjectKeeper<>(account, "obj");
        deleteDirectoryStream(ObjectKeeper.cacheDirectory);
        someData.put("1",new SmartContractData(null, null, "aaa", null,null));
    }


    @After
    public void after() throws IOException {
        deleteDirectoryStream(ObjectKeeper.cacheDirectory);
    }

    @Test
    public void serializeThenDeserialize(){
        objectKeeper.keepObject(someData);
        assertTrue(objectKeeper.getSerializedObjectPath().toFile().exists());

        Map restoredObject = objectKeeper.getKeptObject();
        assertEquals(someData, restoredObject);
    }

    @Test
    public void deserializeThenSerialize() {
        objectKeeper.keepObject(someData);
        objectKeeper.modify(
            objectKeeper.new Modifier(){
            @Override
            public ConcurrentHashMap<String, SmartContractData> modify(
                ConcurrentHashMap<String, SmartContractData> restoredObject) {
                restoredObject.put("2", new SmartContractData(null, null, "BBB", null, null));
                return restoredObject;
            }
        });
        assertEquals(2, someData.size());
    }

    @Test
    public void usingSerializedObject(){
        objectKeeper.keepObject(someData);
        ConcurrentHashMap<String, SmartContractData> restoredObject = objectKeeper.getKeptObject();
        restoredObject.put("2", new SmartContractData(null, null, "BBB", null, null));
        objectKeeper.keepObject(restoredObject);
        restoredObject = objectKeeper.getKeptObject();
        assertEquals(2, restoredObject.size());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void deleteDirectoryStream(Path path) throws IOException {
        if(path.toFile().exists()) {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }
}
