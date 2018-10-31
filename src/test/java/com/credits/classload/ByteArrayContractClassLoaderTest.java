package com.credits.classload;

import com.credits.exception.CompilationException;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

import static com.credits.TestUtils.SimpleInMemoryCompiler;

public class ByteArrayContractClassLoaderTest {

    String sourceCode;

    @Before
    public void setUp() throws Exception {
        sourceCode = "public class Contract {\n" + "\n" + "    public Contract() {\n" +
            "        System.out.println(\"Hello World!!\"); ;\n" + "    }\n" + "}";
        String sourceCode2 =
            "public class HelloWorld extends SomeClass{ \n@Override \npublic void foo(){\nSystem.out.println(\"HelloWorld method called\");\n}\n}\npublic class SomeClass{\npublic void foo()\nSystem.out.println(\"SomeClass method called\");\n}\n}";
    }

    @Test
    public void buildClassTest() throws Exception {
        byte[] bytecode = SimpleInMemoryCompiler.compile(sourceCode, "Contract", "TKN");

        Class clazz = new ByteArrayContractClassLoader().buildClass(bytecode);
        clazz.newInstance();
    }

    @Test(expected = LinkageError.class)
    public void buildClassTwice() throws CompilationException, MalformedURLException {
        byte[] bytecode = SimpleInMemoryCompiler.compile(sourceCode, "Contract", "TKN");

        ByteArrayContractClassLoader loader = new ByteArrayContractClassLoader();
        loader.buildClass(bytecode);
        loader.buildClass(bytecode);
    }

    @Test
    public void loadOtherClass() throws CompilationException, MalformedURLException {
        sourceCode = "public class Contract {\n" + "\n" + "    public Contract() {\n" +
            "try {\n new java.net.ServerSocket(5000);\n} catch (java.io.IOException e) {\ne.printStackTrace();\n}\n" + "    }\n" + "}";

        byte[] bytecode = SimpleInMemoryCompiler.compile(sourceCode, "Contract", "TKN");
        ByteArrayContractClassLoader loader = new ByteArrayContractClassLoader();
        loader.buildClass(bytecode);
    }
}