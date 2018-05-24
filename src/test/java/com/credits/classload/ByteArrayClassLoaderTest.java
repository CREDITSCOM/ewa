package com.credits.classload;

import com.credits.App;
import com.credits.exception.CompilationException;
import com.credits.leveldb.client.data.SmartContractData;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {App.class})
public class ByteArrayClassLoaderTest {

    @Resource
    ByteArrayClassLoader classLoader;

    SmartContractData smartContractData;

    @Before
    public void setUp() throws Exception {
        String sourceCode2 =
            "public class HelloWorld extends SomeClass{ \n@Override \npublic void foo(){\nSystem.out.println(\"HelloWorld method called\");\n}\n}\npublic class SomeClass{\npublic void foo()\nSystem.out.println(\"SomeClass method called\");\n}\n}";
    }

    @Test
    public void getClassTest() throws Exception {
        String sourceCode = "public class HelloWorld {\n" + "\n" + "    public HelloWorld() {\n" +
            "        System.out.println(\"Hello World!!\"); ;\n" + "    }\n" + "}";
        byte[] helloWorldByteCode = SimpleInMemoryCompilator.compile(sourceCode, "HelloWorld", "TKN");
        smartContractData = new SmartContractData(sourceCode, helloWorldByteCode, "hashState");
        System.out.println(Arrays.toString(smartContractData.getByteCode()));
        Class clazz = classLoader.buildClass("HelloWorld", smartContractData.getByteCode());
        clazz.newInstance();
    }

    @Test
    @Ignore
    public void buildMultiplyClass() throws Exception {
        String sourceCode =
            "class TestClass{\npublic static class HelloWorld extends SomeClass{\n@Override\npublic void foo(){\nSystem.out.println(\"HelloWorld method called\");\n}\n}\npublic static class SomeClass{\npublic void foo(){\nSystem.out.println(\"SomeClass method called\");\n}\n}\n}";
        byte[] helloWorldByteCode = SimpleInMemoryCompilator.compile(sourceCode, "SomeClass", "TKN");
        smartContractData = new SmartContractData(sourceCode, helloWorldByteCode, "hashState");
        byte[] byteCode = smartContractData.getByteCode();
        classLoader.loadClass("TestClass", byteCode);
        classLoader.loadClass("SomeClass", byteCode);
        Class clazz = classLoader.buildClass("HelloWorld", byteCode);
        clazz.newInstance();
    }

    public static class SimpleInMemoryCompilator {

        private final static Logger logger = LoggerFactory.getLogger(SimpleInMemoryCompilator.class);
        private final static String SOURCE_FOLDER_PATH =
            System.getProperty("user.dir") + File.separator + "temp" + File.separator;

        public static byte[] compile(String sourceString, String classname, String token) throws CompilationException {
            File sourceFolder = new File(SOURCE_FOLDER_PATH + token);

            File source = save(sourceFolder, classname, sourceString);

            logger.debug("Compiling class {}", source.getName());
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);

            Iterable<? extends JavaFileObject> compilationUnits =
                stdFileManager.getJavaFileObjectsFromFiles(Collections.singletonList(source));

            JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);
            Boolean isCompiled = task.call();

            if (!isCompiled) {
                StringBuilder errorMessage = new StringBuilder("");
                for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                    logger.error("Error on line {} in {}. Message: {}", diagnostic.getLineNumber(),
                        diagnostic.getSource(), diagnostic.getMessage(null));
                    errorMessage.append(String.format("Error on line %d. Message: %s\n", diagnostic.getLineNumber(),
                        diagnostic.getMessage(null)));
                }
                throw new CompilationException(
                    "Cannot compile the file: " + source.getName() + "\n" + errorMessage.toString());
            }

            try {
                stdFileManager.close();
            } catch (IOException e) {
                System.out.println(e);
            }

            byte[] sourceBytes = new byte[0];
            try {
                File classFile = new File(sourceFolder + File.separator + classname + ".class");
                sourceBytes = FileUtils.readFileToByteArray(classFile);
            } catch (IOException e) {
                System.out.println(e);
            }

            for (File file : sourceFolder.listFiles()) {
                file.delete();
            }
            sourceFolder.delete();

            return sourceBytes;

        }

        private static File save(File sourceFolder, String classname, String sourceString) throws CompilationException {
            byte[] sourceBytes = sourceString.getBytes();
            File sourceFile = new File(sourceFolder + File.separator + classname + ".java");
            try {
                FileUtils.writeByteArrayToFile(sourceFile, sourceBytes);
            } catch (IOException e) {
                System.out.println(e);
            }

            return sourceFile;
        }
    }
}