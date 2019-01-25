package com.credits.wallet.desktop.utils;

import com.credits.client.executor.service.ContractExecutorApiServiceImpl;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.VariantData;
import com.credits.general.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.testUtils.WalletTestUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

public class ExecutorIntegrationTest {
    ContractExecutorApiServiceImpl contractExecutorService;
    @Before
    public void setUp() {
        contractExecutorService = ContractExecutorApiServiceImpl.getInstance("127.0.0.1", 9080);
    }


    @Ignore
    @Test
    public void executorIntegrationTest() throws IOException {
        String sourceCode = WalletTestUtils.readSourceCode("/executorIntegrationTest/Contract.java");
        CompilationPackage compilationPackage = SourceCodeBuilder.compileSourceCode(sourceCode).getCompilationPackage();
        List<ByteCodeObjectData> byteCodeObjectDataList =
            GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
        byte[] initiatorAddress = "initiatorAddress".getBytes();
        byte[] contractAddress = "contractAddress".getBytes();
        ExecuteByteCodeResult executeByteCodeResult =
            contractExecutorService.executeContractMethod(initiatorAddress, contractAddress,
                GeneralConverter.byteCodeObjectsDataToByteCodeObjects(byteCodeObjectDataList), "".getBytes(), null,
                null, 600);

        byte[] contractState = executeByteCodeResult.getContractState();

        ExecuteByteCodeResult val = contractExecutorService.executeContractMethod(initiatorAddress, contractAddress,
            GeneralConverter.byteCodeObjectsDataToByteCodeObjects(byteCodeObjectDataList), contractState, "val",
            asList(new VariantData[] {}), 500L);

        int i = val.getRet_val().getV_int_box();
        Assert.assertEquals(i,1);
    }



}
