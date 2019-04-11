package com.credits.general.util.compiler;


import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.general.util.compiler.model.JavaSourceFromString;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

/**
 * The dynamic compiler uses the JavaCompiler with custom implementations of a JavaFileManager and
 * JavaFileObject to compile a Java Source from a String to Bytecode.
 *
 * @see InMemoryClassManager
 * @see JavaSourceFromString
 */
public class InMemoryCompiler {

    /**
     * Compiles a single class.
     *
     * @return Compilation package
     */
    public CompilationPackage compile(Map<String, String> classesToCompile) {
        JavaCompiler compiler = getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> collector = getDiagnosticCollector();
        InMemoryClassManager manager = getClassManager(compiler);

        // java source from string
        List<JavaSourceFromString> strFiles = new ArrayList<>();
        for (String className : classesToCompile.keySet()) {
            String classCode = classesToCompile.get(className);
            strFiles.add(new JavaSourceFromString(className, classCode));
        }

        // add classpath to options
        List<String> options = singletonList("-parameters");


        // compile
        CompilationTask task = compiler.getTask(null, manager, collector, options, null, strFiles);

        boolean status = task.call();

        List<CompilationUnit> compilationUnits = manager.getAllClasses();
        return new CompilationPackage(compilationUnits, collector, status);
    }

    DiagnosticCollector<JavaFileObject> getDiagnosticCollector() {
        return new DiagnosticCollector<>();
    }

    InMemoryClassManager getClassManager(JavaCompiler compiler) {
        return new InMemoryClassManager(compiler.getStandardFileManager(null, null, null));
    }

    JavaCompiler getSystemJavaCompiler() throws CompilationException {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            String jdkPath = this.loadJdkPathFromEnvironmentVariables();
            System.setProperty("java.home", jdkPath);
            compiler = ToolProvider.getSystemJavaCompiler();
        }
        return compiler;
    }

    String loadJdkPathFromEnvironmentVariables() throws CompilationException {
        if (SystemUtils.OS_NAME.toLowerCase().contains("win")) {
            Pattern regexpJdkPath = Pattern.compile("jdk[\\d]\\.[\\d]\\.[\\d]([\\d._])");
            String jdkBinPath = Arrays.stream(System.getenv("Path").split(";"))
                .filter(it -> regexpJdkPath.matcher(it).find())
                .findFirst()
                .orElseThrow(() -> new CompilationException(
                    "Cannot compile the file. The java compiler has not been found, Java Development Kit should be installed."));
            return jdkBinPath.substring(0, jdkBinPath.length() - 4); // remove last 4 symbols "\bin"
        } else {
            return "";
        }
    }

    String loadClasspath() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        URLClassLoader urlClassLoader = (URLClassLoader) Thread
            .currentThread().getContextClassLoader();
        for (URL url : urlClassLoader.getURLs()) {
            sb.append(URLDecoder.decode(url.getFile(), "UTF-8")).append(
                System.getProperty("path.separator"));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static CompilationPackage compileSourceCode(String sourceCode) throws CompilationException {
        Map<String, String> classesToCompile = new HashMap<>();
        String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
        classesToCompile.put(className, sourceCode);

        CompilationPackage compilationPackage = new InMemoryCompiler().compile(classesToCompile);

        if (!compilationPackage.isCompilationStatusSuccess()) {
            DiagnosticCollector collector = compilationPackage.getCollector();
            List<Diagnostic> diagnostics = collector.getDiagnostics();
            List<CompilationException.Error> errors = new ArrayList<>();
            diagnostics.forEach(action -> {
                CompilationException.Error error = new CompilationException.Error(
                    action.getLineNumber(),
                    action.getMessage(null)
                );
                errors.add(error);
            });
            throw new CompilationException(errors);
        }
        return compilationPackage;
    }


}
