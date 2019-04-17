package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.wallet.desktop.struct.MethodSimpleDeclaration;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import com.credits.wallet.desktop.testUtils.WalletTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourceCodeUtilsTest {

    @Test
    public void methodParserTest() throws IOException {
        String sourceCode = WalletTestUtils.readSourceCode("/methodParserTest/Contract.java");
        List<MethodSimpleDeclaration> methodDeclarations = new ArrayList<>();
        ParseResultStruct build = new ParseResultStruct.Builder(sourceCode).methods().build();

        methodDeclarations.addAll(build.methods);
        Assert.assertEquals(methodDeclarations.size(),2);
        Assert.assertEquals(methodDeclarations.get(0).getMethodDeclaration().getName().getIdentifier(),"initialize");
        Assert.assertEquals(methodDeclarations.get(1).getMethodDeclaration().getName().getIdentifier(),"balanceGet");

    }

}
