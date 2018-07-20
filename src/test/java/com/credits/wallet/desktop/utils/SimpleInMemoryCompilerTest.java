package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.exception.CompilationException;
import org.junit.Assert;
import org.junit.Test;

public class SimpleInMemoryCompilerTest {

    @Test
    public void compileTest() throws CompilationException {
        String source =  "public class Contract extends SmartContract { public Contract() { total = 0; } }";
        byte[] bytes = SimpleInMemoryCompiler.compile(source, "Contract", "123");
        Assert.assertNotNull(bytes);
        Assert.assertNotEquals(0, bytes.length);
    }
}
