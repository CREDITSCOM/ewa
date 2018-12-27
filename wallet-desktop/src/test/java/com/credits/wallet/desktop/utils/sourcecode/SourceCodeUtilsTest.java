package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.wallet.desktop.testUtils.WalletTestUtils;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SourceCodeUtilsTest {

    @Test
    public void methodParserTest() throws IOException {
        String sourceCode = WalletTestUtils.readSourceCode("/methodParserTest/Contract.java");
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        for (BodyDeclaration parseMethod : ParseCodeUtils.parseMethods(sourceCode)) {
            methodDeclarations.add((MethodDeclaration)parseMethod);
        }

        Assert.assertEquals(methodDeclarations.size(),2);
        Assert.assertEquals(methodDeclarations.get(0).getName().getIdentifier(),"initialize");
        Assert.assertEquals(methodDeclarations.get(1).getName().getIdentifier(),"balanceGet");

    }

}
