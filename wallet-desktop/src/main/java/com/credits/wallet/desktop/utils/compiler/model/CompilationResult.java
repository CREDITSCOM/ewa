package com.credits.wallet.desktop.utils.compiler.model;

import com.credits.wallet.desktop.struct.ErrorCodeTabRow;

import java.util.List;

public class CompilationResult {
    private List<ErrorCodeTabRow> errors;
    private CompilationPackage compilationPackage;

    public CompilationResult(CompilationPackage compilationPackage, List<ErrorCodeTabRow> listOfError) {
        this.errors = listOfError;
        this.compilationPackage = compilationPackage;
    }

    public List<ErrorCodeTabRow> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorCodeTabRow> errors) {
        this.errors = errors;
    }

    public CompilationPackage getCompilationPackage() {
        return compilationPackage;
    }

    public void setCompilationPackage(CompilationPackage compilationPackage) {
        this.compilationPackage = compilationPackage;
    }
}