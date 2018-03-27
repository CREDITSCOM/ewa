package com.credits.wallet.desktop.utils;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;

/**
 * Created by Rustem Saidaliyev on 27-Mar-18.
 */
public class EclipseJdt {

    public static IProblem[] checkSyntax(String sourceCode) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        return compilationUnit.getProblems();
    }

    public static CompilationUnit createCompilationUnit(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.JLS9);
        Map<String, String> compilerOptions = JavaCore.getOptions();
        compilerOptions.put("org.eclipse.jdt.core.compiler.source", "9");
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(true);
        parser.setCompilerOptions(compilerOptions);
        return (CompilationUnit) parser.createAST(null);
    }

}
