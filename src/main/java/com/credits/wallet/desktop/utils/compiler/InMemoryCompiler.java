package com.credits.wallet.desktop.utils.compiler;

import com.credits.wallet.desktop.utils.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.utils.compiler.model.CompilationUnit;
import com.credits.wallet.desktop.utils.compiler.model.JavaMemoryObject;
import com.credits.wallet.desktop.utils.compiler.model.JavaSourceFromString;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The dynamic compiler uses the JavaCompiler with custom implementations of a JavaFileManager and
 * JavaFileObject to compile a Java Source from a String to Bytecode.
 * 
 * @see InMemoryClassManager
 * @see JavaSourceFromString
 * @see JavaMemoryObject
 */
public class InMemoryCompiler {

	/**
	 * Compiles a single class.
	 *
	 * @param className class name
	 * @param code class code
	 * @return Compilation package
	 */
	public CompilationPackage compile(String className, String code){
		JavaCompiler compiler = getSystemJavaCompiler();

		DiagnosticCollector<JavaFileObject> collector = getDiagnosticCollector();
		InMemoryClassManager manager = getClassManager(compiler);

		// defining classpath
		String classpath = loadClasspath();

		// add classpath to options
		List<String> options = Arrays.asList("-classpath", classpath);

		// java source from string
		List<JavaSourceFromString> strFiles = new ArrayList();
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

	JavaCompiler getSystemJavaCompiler() {
		return ToolProvider.getSystemJavaCompiler();
	}

	String loadClasspath() {
		StringBuilder sb = new StringBuilder();
		URLClassLoader urlClassLoader = (URLClassLoader) Thread
				.currentThread().getContextClassLoader();
		for (URL url : urlClassLoader.getURLs()) {
			sb.append(url.getFile()).append(
					System.getProperty("path.separator"));
		}

		return sb.toString();
	}
}
