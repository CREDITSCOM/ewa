package com.credits.wallet.desktop.utils.sourcecode.building;

import com.credits.general.util.compiler.model.CompilationPackage;

import java.util.List;

public class CompilationResult {
    private List<BuildSourceCodeError> errors;
    private CompilationPackage compilationPackage;

    public CompilationResult(CompilationPackage compilationPackage, List<BuildSourceCodeError> listOfError) {
        this.errors = listOfError;
        this.compilationPackage = compilationPackage;
    }

    public List<BuildSourceCodeError> getErrors() {
        return errors;
    }

    public void setErrors(List<BuildSourceCodeError> errors) {
        this.errors = errors;
    }

    public CompilationPackage getCompilationPackage() {
        return compilationPackage;
    }

    public void setCompilationPackage(CompilationPackage compilationPackage) {
        this.compilationPackage = compilationPackage;
    }
}