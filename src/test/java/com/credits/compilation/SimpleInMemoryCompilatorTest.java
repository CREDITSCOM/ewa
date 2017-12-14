package com.credits.compilation;

import com.credits.exception.CompilationException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class SimpleInMemoryCompilatorTest {

    private SimpleInMemoryCompilator compilator;

    @Before
    public void setUp() {
        compilator = new SimpleInMemoryCompilator();
    }

    @Test
    public void compile() {
        URL resource = getClass().getClassLoader().getResource("com/credits/compilation/Test.java");
        File source = new File(resource.getFile());
        try {
            compilator.compile(source);
        } catch (CompilationException e) {
            e.printStackTrace();
        }
    }
}
