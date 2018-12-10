package com.credits.wallet.desktop.utils.sourcecode.building;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.utils.sourcecode.EclipseJdt;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import org.eclipse.jdt.core.compiler.IProblem;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import java.util.ArrayList;
import java.util.List;

public class SourceCodeBuilder {
    public static CompilationResult compileSourceCode(String sourceCode) {
        CompilationPackage compilationPackage = null;
        String className = SourceCodeUtils.parseClassName(sourceCode, "SmartContract");
        List<BuildSourceCodeError> errorsList = new ArrayList<>();
        try {
            SourceCodeUtils.checkClassAndSuperclassNames(className, sourceCode);
        } catch (CreditsException e) {
            BuildSourceCodeError tr = new BuildSourceCodeError();
            tr.setLine("1");
            tr.setText(e.getMessage());
            errorsList.add(tr);
        }
        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);
        if (problemArr.length > 0) {

            for (IProblem p : problemArr) {
                BuildSourceCodeError tr = new BuildSourceCodeError();
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
                    BuildSourceCodeError tr = new BuildSourceCodeError();
                    tr.setLine(GeneralConverter.toString(action.getLineNumber()));
                    tr.setText(action.getMessage(null));
                    errorsList.add(tr);
                });
            }
        }
        return new CompilationResult(compilationPackage,errorsList);
    }
}
