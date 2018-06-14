package com.credits.compilation;

import com.credits.exception.CompilationException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class SimpleInMemoryCompilerTest {

    private SimpleInMemoryCompiler compiler;

    @Before
    public void setUp() {
        compiler = new SimpleInMemoryCompiler();
    }

    @Test
    public void compile() throws CompilationException {
        URL resource = getClass().getClassLoader().getResource("com/credits/compilation/Test.java");
        File source = new File(resource.getFile());
        compiler.compile(source);
    }
}
