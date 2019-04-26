package com.credits.wallet.desktop.struct;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.Serializable;
import java.util.Objects;

public class MethodSimpleDeclaration implements Serializable {
    private static final long serialVersionUID = -8894597100523042369L;

    final MethodDeclaration methodDeclaration;

    public MethodSimpleDeclaration(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodSimpleDeclaration)) {
            return false;
        }
        MethodSimpleDeclaration that = (MethodSimpleDeclaration) o;
        return Objects.equals(methodDeclaration, that.methodDeclaration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodDeclaration);
    }

    @Override
    public String toString() {
        return methodDeclaration.toString().replaceAll("(@\\w*)|((^)? ?public )","");
    }
}
