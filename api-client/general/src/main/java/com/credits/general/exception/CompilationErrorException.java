package com.credits.general.exception;

import java.util.List;

public class CompilationErrorException extends Exception {
    private List<Error> errors;

    public CompilationErrorException(List<Error> errors) {
        super();
        this.errors = errors;
    }

    public static class Error {
        private long lineNumber;
        private String errorMessage;

        public Error(long lineNumber, String errorMessage) {
            this.lineNumber = lineNumber;
            this.errorMessage = errorMessage;
        }

        public long getLineNumber() {
            return lineNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public List<Error> getErrors() {
        return errors;
    }
}
