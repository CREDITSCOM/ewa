package com.credits.wallet.desktop.utils.compiler;


import com.credits.general.exception.CompilationException;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.Converter;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.utils.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.utils.compiler.model.CompilationResult;
import com.credits.wallet.desktop.utils.compiler.model.CompilationUnit;
import com.credits.wallet.desktop.utils.compiler.model.JavaSourceFromString;
import com.credits.wallet.desktop.utils.sourcecode.EclipseJdt;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import org.eclipse.jdt.core.compiler.IProblem;

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
import java.util.List;
import java.util.regex.Pattern;

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
	 * @param className class name
	 * @param code class code
	 * @return Compilation package
	 */
	public CompilationPackage compile(String className, String code) throws CompilationException {
		JavaCompiler compiler = getSystemJavaCompiler();

		DiagnosticCollector<JavaFileObject> collector = getDiagnosticCollector();
		InMemoryClassManager manager = getClassManager(compiler);

		// defining classpath
		String classpath;
		try {
			classpath = loadClasspath();
		} catch (UnsupportedEncodingException e) {
			throw new CompilationException(e);
		}

		// add classpath to options
		List<String> options = Arrays.asList("-classpath", classpath);

		// java source from string
		List<JavaSourceFromString> strFiles = new ArrayList<>();
		strFiles.add(new JavaSourceFromString(className, code) );

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
		Pattern regexpJdkPath = Pattern.compile("jdk[\\d]\\.[\\d]\\.[\\d]([\\d._])");
		String jdkBinPath = Arrays.stream(System.getenv("Path").split(";"))
				.filter(it -> regexpJdkPath.matcher(it).find())
				.findFirst()
				.orElseThrow(() -> new CompilationException("Cannot compile the file. The java compiler has not been found, Java Development Kit should be installed."));
		return jdkBinPath.substring(0, jdkBinPath.length() - 4); // remove last 4 symbols "\bin"
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

    public static CompilationResult compileSourceCode(String sourceCode) {
        CompilationPackage compilationPackage = null;
        String className = SourceCodeUtils.parseClassName(sourceCode, "SmartContract");
        List<ErrorCodeTabRow> errorsList = new ArrayList<>();
        try {
            SourceCodeUtils.checkClassAndSuperclassNames(className, sourceCode);
        } catch (CreditsException e) {
            ErrorCodeTabRow tr = new ErrorCodeTabRow();
            tr.setLine("1");
            tr.setText(e.getMessage());
            errorsList.add(tr);
        }
        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);
        if (problemArr.length > 0) {

            for (IProblem p : problemArr) {
                ErrorCodeTabRow tr = new ErrorCodeTabRow();
                tr.setLine(Integer.toString(p.getSourceLineNumber()));
                tr.setText(p.getMessage());
                errorsList.add(tr);
            }
        } else {
            compilationPackage = new InMemoryCompiler().compile(className, sourceCode);
            if (!compilationPackage.isCompilationStatusSuccess()) {
                DiagnosticCollector collector = compilationPackage.getCollector();
                List<Diagnostic> diagnostics = collector.getDiagnostics();
                diagnostics.forEach(action -> {
                    ErrorCodeTabRow tr = new ErrorCodeTabRow();
                    tr.setLine(Converter.toString(action.getLineNumber()));
                    tr.setText(action.getMessage(null));
                    errorsList.add(tr);
                });
            }
        }
        return new CompilationResult(compilationPackage,errorsList);
    }

}
