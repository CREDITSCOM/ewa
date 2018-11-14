package com.credits.wallet.desktop.utils.compiler.model;

import com.credits.wallet.desktop.struct.ErrorCodeTabRow;

import java.util.List;

public class CompilationResult {
    private List<ErrorCodeTabRow> errorCodeTabRows;
    private CompilationPackage compilationPackage;

    public CompilationResult(CompilationPackage compilationPackage, List<ErrorCodeTabRow> listOfError) {
        this.errorCodeTabRows = listOfError;
        this.compilationPackage = compilationPackage;
    }

    public List<ErrorCodeTabRow> getErrors() {
        return errorCodeTabRows;
    }

    public void setErrorCodeTabRows(List<ErrorCodeTabRow> errorCodeTabRows) {
        this.errorCodeTabRows = errorCodeTabRows;
    }

    public CompilationPackage getCompilationPackage() {
        return compilationPackage;
    }

    public void setCompilationPackage(CompilationPackage compilationPackage) {
        this.compilationPackage = compilationPackage;
    }
}