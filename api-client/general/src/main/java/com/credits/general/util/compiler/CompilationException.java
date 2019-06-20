package com.credits.general.util.compiler;

import com.credits.general.exception.CreditsException;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.joining;

public class CompilationException extends CreditsException {
    private static final long serialVersionUID = 6535841421292200543L;
    private List<Error> errors;

    @SuppressWarnings("unchecked")
    public CompilationException(String message) {
        this(message, EMPTY_LIST);
    }

    public CompilationException(List<Error> errors) {
        this("compilation errors:\n" + errors.stream().map(error -> error.lineNumber + ":" + error.errorMessage).collect(joining("\n")));
    }

    public CompilationException(String message, List<Error> errors) {
        super(message);
        this.errors = errors;
    }

    public static class Error implements Serializable {
        private static final long serialVersionUID = -3815357061263341409L;
        private final long lineNumber;
        private final String errorMessage;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Error)) {
                return false;
            }
            Error error = (Error) o;
            return lineNumber == error.lineNumber &&
                Objects.equals(errorMessage, error.errorMessage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lineNumber, errorMessage);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Error{");
            sb.append("lineNumber=").append(lineNumber);
            sb.append(", errorMessage='").append(errorMessage).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
