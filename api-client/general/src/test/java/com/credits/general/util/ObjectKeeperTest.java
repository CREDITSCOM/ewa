package com.credits.general.util;

import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.thrift.generated.TokenStandart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
public class ObjectKeeperTest {

    HashMap<String, SmartContractData> someData = new HashMap<>();
    ObjectKeeper<HashMap<String,SmartContractData>> objectKeeper;
    String account = "G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ";

    @Before
    public void setUp() throws IOException {
        objectKeeper = new ObjectKeeper<>(account, "obj");
        deleteCacheDirectory();
        SmartContractDeployData smartContractDeployData = new SmartContractDeployData("aaa", null, TokenStandart.CreditsBasic);
        someData.put("1",new SmartContractData(null, null, smartContractDeployData,null));
    }


    @After
    public void after() throws IOException {
        deleteCacheDirectory();
    }

    @Test
    public void serializeThenDeserialize(){
        objectKeeper.keepObject(someData);
        assertFalse(objectKeeper.getSerializedObjectPath().toFile().exists());
        objectKeeper.flush();
        assertTrue(objectKeeper.getSerializedObjectPath().toFile().exists());

        Map restoredObject = objectKeeper.getKeptObject().orElseGet(HashMap::new);
        assertEquals(someData, restoredObject);
    }

    @Test
    public void deserializeThenSerialize() {
        objectKeeper.keepObject(someData);
        objectKeeper.modify(
            objectKeeper.new Modifier() {
                @Override
                public HashMap<String, SmartContractData> modify(HashMap<String, SmartContractData> keptObject) {
                    if (keptObject != null) {
                        SmartContractDeployData smartContractDeployData = new SmartContractDeployData("BBB", null, TokenStandart.CreditsBasic);
                        keptObject.put("2", new SmartContractData(null, null, smartContractDeployData, null));
                    }
                    return keptObject;
                }
            });
        assertEquals(2, objectKeeper.getKeptObject().get().size());
    }

    @Test
    public void usingSerializedObject(){
        objectKeeper.keepObject(someData);
        HashMap<String, SmartContractData> restoredObject = objectKeeper.getKeptObject().get();
        SmartContractDeployData smartContractDeployData = new SmartContractDeployData("BBB", null, TokenStandart.CreditsBasic);
        restoredObject.put("2", new SmartContractData(null, null, smartContractDeployData, null));
        objectKeeper.keepObject(restoredObject);
        restoredObject = objectKeeper.getKeptObject().get();
        assertEquals(2, restoredObject.size());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void deleteCacheDirectory() throws IOException {
        if(ObjectKeeper.cacheDirectory.toFile().exists()) {
            Files.walk(ObjectKeeper.cacheDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }
}
