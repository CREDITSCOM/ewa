package com.credits.general.util.sourceCode;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

public class GeneralSourceCodeUtils {

    public static String parseClassName(String sourceCode) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        List typeList = compilationUnit.types();
        if (typeList.size() != 1) {
            return null;
        }
        TypeDeclaration typeDeclaration = (TypeDeclaration) typeList.get(0);
        return (typeDeclaration).getName().getFullyQualifiedName();
    }
}
