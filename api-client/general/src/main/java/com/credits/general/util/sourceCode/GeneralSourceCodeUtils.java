package com.credits.general.util.sourceCode;

import org.apache.commons.lang3.tuple.Pair;
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

    public static Pair<String, String> splitClassnameAndGeneric(String classNameWithGeneric) {
        int openingBracketPosition = classNameWithGeneric.indexOf("<");
        boolean isGenericExists = (openingBracketPosition > -1);
        String classNameWithoutGeneric;
        String genericName = null;
        if (isGenericExists) {
            classNameWithoutGeneric = classNameWithGeneric.substring(0, openingBracketPosition);
            int closingBracketPosition = classNameWithGeneric.indexOf(">", openingBracketPosition);
            genericName = classNameWithGeneric.substring(openingBracketPosition + 1, closingBracketPosition);
        } else {
            classNameWithoutGeneric = classNameWithGeneric;
        }
        return Pair.of(classNameWithoutGeneric, genericName);
    }

    public static String parseArrayType(String arrayDeclaration) {
        return arrayDeclaration.substring(0, arrayDeclaration.indexOf("[]"));
    }
}
