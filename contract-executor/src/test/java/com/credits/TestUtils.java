package com.credits;

import com.credits.exception.CompilationException;
import com.credits.general.exception.CreditsException;
import org.apache.commons.io.FileUtils;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.Collections.singletonList;

public class TestUtils {
    public static String encrypt(byte[] bytes) throws CreditsException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CreditsException(e);
        }
        digest.update(bytes);
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static class SimpleInMemoryCompiler {

        private final static Logger logger = LoggerFactory.getLogger(SimpleInMemoryCompiler.class);
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
                stdFileManager.getJavaFileObjectsFromFiles(singletonList(source));

            JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, singletonList("-parameters"), null, compilationUnits);
            Boolean isCompiled = task.call();

            if (!isCompiled) {
                StringBuilder errorMessage = new StringBuilder();
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

        private static File save(File sourceFolder, String classname, String sourceString) {
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
