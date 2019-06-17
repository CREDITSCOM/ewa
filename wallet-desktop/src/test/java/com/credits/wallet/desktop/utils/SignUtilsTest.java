package com.credits.wallet.desktop.utils;

import com.credits.client.node.crypto.Ed25519;
import com.credits.client.node.pojo.TransactionFlowData;
import com.credits.client.node.util.SignUtils;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;

public class SignUtilsTest {

    private String privKey = "3rUevsW5xfob6qDxWMDFwwTQCq39SYhzstuyfUGSDvF2QHBRyPD8fSk49wFXaPk3GztfxtuU85QHfMV3ozfqa7rN";

    @Test
    public void signUtilsTestSmartContractTransactionFlow() throws CreditsException, IOException {
        AppState.setPrivateKey(getPrivateKey());


        long id = 211106232532995L;
        byte[] source = GeneralConverter.decodeFromBASE58("3xyZh");
        byte[] target = GeneralConverter.decodeFromBASE58("FpspT");
        BigDecimal amount = new BigDecimal(0);
        short offeredMaxFee = 0;


        byte[] smartContractBytes = GeneralConverter.decodeFromBASE58(Files.readAllLines(Paths.get(
            "src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "SignUtilsTest")).get(0));

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(id, source, target, amount, offeredMaxFee, smartContractBytes, null);
        SignUtils.signTransaction(transactionFlowData,AppState.getPrivateKey());
        Assert.assertEquals("47WZiPHggsXydSvGyRwDHh1nVfsxPYQrn9kczkg4tvgoMHdm9KbTvRHnSSHEejMdnoMYu94ZnboP15hEweuNuqma",
            GeneralConverter.encodeToBASE58(transactionFlowData.getSignature()));
    }

    private PrivateKey getPrivateKey() {
        byte[] privateKeyByteArr = GeneralConverter.decodeFromBASE58(privKey);
        PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        Assert.assertEquals(privateKey.hashCode(), -2062620138);
        return privateKey;
    }

    @Test
    public void signUtilsTestTransactionFlow() throws CreditsException {
        String privKey = "3rUevsW5xfob6qDxWMDFwwTQCq39SYhzstuyfUGSDvF2QHBRyPD8fSk49wFXaPk3GztfxtuU85QHfMV3ozfqa7rN";
        AppState.setPrivateKey(getPrivateKey());
        long id = 140737488355333L;
        byte[] source = GeneralConverter.decodeFromBASE58("3xyZh");
        byte[] target = GeneralConverter.decodeFromBASE58("5B3YXqDTcWQFGAqEJQJP3Bg1ZK8FFtHtgCiFLT5VAxpd");
        BigDecimal amount = new BigDecimal(111);
        short offeredMaxFee = 26184;
        byte[] smartContractBytes = null;

        TransactionFlowData transactionFlowData =
            new TransactionFlowData(id, source, target, amount, offeredMaxFee, smartContractBytes, null);
        SignUtils.signTransaction(transactionFlowData,AppState.getPrivateKey());
        Assert.assertEquals("2jDcD15fkTb4z3Yic1s4hZqNzwtEBVHjncfmRZpFJVztnLNKZHurQPPJHK8NeyFRiQrsxNSrZdKXYXiANnmAbN9f",
            GeneralConverter.encodeToBASE58(transactionFlowData.getSignature()));
    }
}
