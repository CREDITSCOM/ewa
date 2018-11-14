package com.credits.wallet.desktop.utils.compiler.model;

import com.google.common.base.Objects;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

/**
 * A compilation package compiles a list of compilation units. This is particularly useful
 * when you have (anonymous) inner classes inside your compiled code, in which case this package
 * will contain the main class and the anonymous classes, which allow all of them to be loaded
 * as a package when necessary.
 *
 * <p>
 * Example:
 * - MyClass
 * - MyClass$1
 * - MyClass$MyInnerClass
 * </p>
 */
public class CompilationPackage {
	private final List<CompilationUnit> units;
	private final DiagnosticCollector<JavaFileObject> collector;
	private final boolean compilationStatus;

	public CompilationPackage(List<CompilationUnit> units, DiagnosticCollector<JavaFileObject> collector, boolean compilationStatus) {
		this.units = new ArrayList(units);
		this.collector = collector;
        this.compilationStatus = compilationStatus;
    }

	public List<CompilationUnit> getUnits() {
		return units;
	}

	public DiagnosticCollector<JavaFileObject> getCollector() {
		return collector;
	}

    public boolean isCompilationStatusSuccess() {
        return compilationStatus;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CompilationPackage that = (CompilationPackage) o;
		return Objects.equal(units, that.units);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(units);
	}
}
