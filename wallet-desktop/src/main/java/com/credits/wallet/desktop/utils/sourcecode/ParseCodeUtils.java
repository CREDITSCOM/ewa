package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.sourceCode.EclipseJdt;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseCodeUtils {

    private static final String CLASS_NAME = "Contract";
    private static final String SUPERCLASS_NAME = "SmartContract";


    public static List<BodyDeclaration> parseFieldsConstructorsMethods(String sourceCode){
        List<BodyDeclaration> list = new ArrayList<>();
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        List typeList = compilationUnit.types();
        if (typeList.size() != 1) {
            return list;
        }
        ASTNode root = compilationUnit.getRoot();
        root.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration typeNote) {
                list.addAll(getConstructors(typeNote));
                list.addAll(getMethods(typeNote));
                list.addAll(getFields(typeNote));

                return false;
            }
        });

        return list;
    }

    public static String parseSuperclassName(String sourceCode) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        List typeList = compilationUnit.types();
        if (typeList.size() != 1) {
            return null;
        }
        TypeDeclaration typeDeclaration = (TypeDeclaration) typeList.get(0);
        Type superclassType = typeDeclaration.getSuperclassType();
        if (superclassType == null) {
            throw new CreditsException("Superclass is not exists");
        }
        return ((SimpleType)superclassType).getName().getFullyQualifiedName();
    }


    public static List<FieldDeclaration> parseFields(String sourceCode) {
        List<FieldDeclaration> list = new ArrayList<>();
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        List typeList = compilationUnit.types();
        if (typeList.size() != 1) {
            return list;
        }
        ASTNode root = compilationUnit.getRoot();
        root.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration typeNote) {
                list.addAll(getFields(typeNote));
                return false;
            }
        });
        return list;
    }



    public static List<MethodDeclaration> parseMethods(String sourceCode) {
        List<MethodDeclaration> list = new ArrayList<>();
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        List typeList = compilationUnit.types();
        if (typeList.size() != 1) {
            return list;
        }
        ASTNode root = compilationUnit.getRoot();
        root.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration typeNote) {
                list.addAll(getMethods(typeNote));
                return false;
            }
        });
        return list;
    }

    private static List<MethodDeclaration> getMethods(TypeDeclaration typeNote) {
        List<MethodDeclaration> list = new ArrayList<>();
        MethodDeclaration[] notes = typeNote.getMethods();
        for (MethodDeclaration note : notes) {
            if(Modifier.isStatic(note.getModifiers()) || Modifier.isPrivate(note.getModifiers()) || note.isConstructor()) {
                continue;
            }
            note.setBody(null);
            note.setJavadoc(null);
            list.add(note);
        }
        return list;
    }

    private static List<FieldDeclaration> getFields(TypeDeclaration typeNote) {
        FieldDeclaration[] notes = typeNote.getFields();
        for (FieldDeclaration note : notes) {
            for (Object fragment : note.fragments()) {
                    ((VariableDeclarationFragment) fragment).setInitializer(null);
                }
        }
        return new ArrayList<>(Arrays.asList(notes));
    }

    public static String parseClassName(SingleVariableDeclaration singleVariableDeclaration) {
        Type type = singleVariableDeclaration.getType();
        return ParseCodeUtils.parseClassName(type);
    }

    private static String parseClassName(Type type) {
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

    public static Pair<Map<MethodDeclaration, String>, Map<FieldDeclaration, String>> getMethodsAndFieldsFromSourceCode(
        String sourceCode) {
        Map<MethodDeclaration, String> methodDeclarationMap = new HashMap<>();
        Map<FieldDeclaration, String> fieldDeclarationMap = new HashMap<>();

        List<FieldDeclaration> fields = parseFields(sourceCode);
        fields.forEach(field -> fieldDeclarationMap.put(field, field.toString()));

        List<MethodDeclaration> methods = parseMethods(sourceCode);
        methods.forEach(method -> {
            method.setBody(null);
            methodDeclarationMap.put(method, method.toString());
        });

        return Pair.of(methodDeclarationMap, fieldDeclarationMap);
    }


    public static List<MethodDeclaration> parseConstructors(String sourceCode) {
        List<MethodDeclaration> list = new ArrayList<>();
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        List typeList = compilationUnit.types();
        if (typeList.size() != 1) {
            return list;
        }
        ASTNode root = compilationUnit.getRoot();
        root.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration typeNote) {
                list.addAll(getConstructors(typeNote));
                return false;
            }
        });
        return list;
    }

    private static List<MethodDeclaration> getConstructors(TypeDeclaration typeNote) {
        List<MethodDeclaration> list = new ArrayList<>();
        MethodDeclaration[] notes = typeNote.getMethods();
        for (MethodDeclaration note : notes) {
            if (note.isConstructor()) {
                note.setBody(null);
                note.setJavadoc(null);
                list.add(note);
            }
        } return list;
    }

    public static int getLineNumber(String sourceCode, BodyDeclaration bodyDeclaration) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        return compilationUnit.getLineNumber(bodyDeclaration.getStartPosition());
    }

    public static void checkClassAndSuperclassNames(String className, String sourceCode) throws CreditsException {
        String superclassName = parseSuperclassName(sourceCode);

        if (superclassName == null || !superclassName.equals(SUPERCLASS_NAME)) {
            throw new CreditsException(
                String.format("Wrong superclass name %s, superclass name must be %s", superclassName, SUPERCLASS_NAME));
        }
    }
}
