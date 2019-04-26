package com.credits.wallet.desktop.utils.sourcecode.building;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.compiler.model.JavaSourceFromString;
import com.credits.general.util.sourceCode.EclipseJdt;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import org.eclipse.jdt.core.compiler.IProblem;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.tools.Diagnostic.Kind.ERROR;

public class SourceCodeBuilder {


    public static CompilationResult compileSourceCode(List<String> sourceCodes) {
        return compileSourceCode(sourceCodes, new ArrayList<>());
    }

    public static CompilationResult compileSourceCode(List<String> sourceCodes, List<BuildSourceCodeError> errorsList) {
        Map<String, String> classesToCompile = new HashMap<>();
        sourceCodes.forEach((sourceCode) -> {
            String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
            classesToCompile.put(className, sourceCode);
            errorsList.addAll(checkSyntax(sourceCode, className));
        });
        if (errorsList.size() > 0) {
            return new CompilationResult(null, errorsList);
        }
        return compileClasses(classesToCompile, errorsList);
    }

    @SuppressWarnings("unchecked")
    private static CompilationResult compileClasses(Map<String, String> classesToCompile, List<BuildSourceCodeError> errorsList) {
        CompilationPackage compilationPackage = new InMemoryCompiler().compile(classesToCompile);
        DiagnosticCollector collector = compilationPackage.getCollector();
        List<Diagnostic> diagnostics = collector.getDiagnostics();
        diagnostics.forEach(action -> {
            if(action.getKind() == ERROR) {
                String className = action.getSource() != null
                    ? ((JavaSourceFromString) action.getSource()).getName()
                    : "";
                BuildSourceCodeError tr =
                    new BuildSourceCodeError(className, Math.toIntExact(action.getLineNumber()), action.getMessage(null));
                errorsList.add(tr);
            }
        });
        return new CompilationResult(compilationPackage, errorsList);
    }

    private static List<BuildSourceCodeError> checkSyntax(String sourceCode, String className) {
        List<BuildSourceCodeError> errorsList = new ArrayList<>();
        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);
        if (problemArr.length > 0) {
            for (IProblem p : problemArr) {
                BuildSourceCodeError tr = new BuildSourceCodeError(className, p.getSourceLineNumber(), p.getMessage());
                errorsList.add(tr);
            }
        }
        return errorsList;
    }

    public static CompilationResult compileSmartSourceCode(String sourceCode) {
        List<BuildSourceCodeError> errorsList = new ArrayList<>();
        //        errorsList.addAll(checkSuperClassNames(sourceCode,errorsList));
        return compileSourceCode(Collections.singletonList(sourceCode), errorsList);
    }

    private static List<BuildSourceCodeError> checkSuperClassNames(
        String sourceCode,
        List<BuildSourceCodeError> errorsList) {
        String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
        try {
            ParseCodeUtils.checkClassAndSuperclassNames(sourceCode);
        } catch (CreditsException e) {
            BuildSourceCodeError tr = new BuildSourceCodeError(className, 1, e.getMessage());
            errorsList.add(tr);
        }
        return errorsList;
    }
}