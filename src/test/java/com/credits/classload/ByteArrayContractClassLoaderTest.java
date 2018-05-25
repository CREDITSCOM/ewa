package com.credits.classload;

import com.credits.exception.CompilationException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
        byte[] bytecode = SimpleInMemoryCompilator.compile(sourceCode, "Contract", "TKN");

        Class clazz = new ByteArrayContractClassLoader().buildClass(bytecode);
        clazz.newInstance();
    }

    @Test(expected = LinkageError.class)
    public void buildClassTwice() throws CompilationException {
        byte[] bytecode = SimpleInMemoryCompilator.compile(sourceCode, "Contract", "TKN");

        ByteArrayContractClassLoader loader = new ByteArrayContractClassLoader();
        loader.buildClass(bytecode);
        loader.buildClass(bytecode);
    }

    @Test
    public void loadOtherClass() throws CompilationException {
        sourceCode = "public class Contract {\n" + "\n" + "    public Contract() {\n" +
            "try {\n new java.net.ServerSocket(5000);\n} catch (java.io.IOException e) {\ne.printStackTrace();\n}\n" + "    }\n" + "}";

        byte[] bytecode = SimpleInMemoryCompilator.compile(sourceCode, "Contract", "TKN");
        ByteArrayContractClassLoader loader = new ByteArrayContractClassLoader();
        loader.buildClass(bytecode);
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