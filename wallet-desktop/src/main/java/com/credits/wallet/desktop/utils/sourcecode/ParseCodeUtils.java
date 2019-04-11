package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.sourceCode.EclipseJdt;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import java.util.List;

public class ParseCodeUtils {

    private static final String SUPERCLASS_NAME = "com.credits.scapi.annotations.SmartContract";

    public static int getLineNumber(String sourceCode, BodyDeclaration bodyDeclaration) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        return compilationUnit.getLineNumber(bodyDeclaration.getStartPosition());
    }

    public static String parseClassName(Type type) {
        if (type.isSimpleType()) {
            SimpleType simpleType = (SimpleType)type;
            return simpleType.getName().getFullyQualifiedName();
        } else if (type.isPrimitiveType()) {
            PrimitiveType primitiveType = (PrimitiveType)type;
            return primitiveType.getPrimitiveTypeCode().toString();
        } else if (type.isParameterizedType()) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            SimpleType simpleType = (SimpleType)parameterizedType.getType();
            String simpleTypeName = simpleType.getName().getFullyQualifiedName();
            List parameterizedTypeArgumentList = parameterizedType.typeArguments();
            if (parameterizedTypeArgumentList.size() == 1) {
                String argumentClassName = ((SimpleType)parameterizedTypeArgumentList.get(0)).getName().getFullyQualifiedName();
                return String.format("%s<%s>", simpleTypeName, argumentClassName);
            } else {
                return simpleTypeName;
            }
        } else if (type.isArrayType()) {
            ArrayType arrayType = (ArrayType)type;
            Type elementType = arrayType.getElementType();
            return ParseCodeUtils.parseClassName(elementType) + "[]";
        }
        throw new IllegalArgumentException(String.format("Unsupported org.eclipse.jdt.core.dom.Type class: %s", type.getClass().getName()));
    }

    public static void checkClassAndSuperclassNames(String sourceCode) throws CreditsException {
        ParseResultStruct build =
            new ParseResultStruct.Builder(sourceCode).superClassName().build();

        String superclassName = build.superClass;

        if (superclassName == null || !superclassName.equals(SUPERCLASS_NAME)) {
            throw new CreditsException(
                String.format("Wrong superclass name %s, superclass name must be %s", superclassName, SUPERCLASS_NAME));
        }
    }
}
