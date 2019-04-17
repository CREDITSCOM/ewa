package com.credits.wallet.desktop.struct;

import com.credits.general.util.sourceCode.EclipseJdt;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParseResultStruct {


    public List<MethodSimpleDeclaration> methods=new ArrayList<>();
    public List<FieldDeclaration> fields=new ArrayList<>();
    public List<MethodSimpleDeclaration> constructors = new ArrayList<>();
    public List<String> interfaces = new ArrayList<>();
    public String superClass;
    public String currentClass;



    public static class Builder {
        private String sourceCode;
        private boolean methods;
        private boolean fields;
        private boolean constructors;
        private boolean interfaces;
        private boolean superClass;
        private boolean currentClass;

        public Builder(String sourceCode) {
            this.sourceCode = sourceCode;
        }

        public Builder methods() {
            methods = true;
            return this;
        }

        public Builder fields() {
            fields = true;
            return this;
        }

        public Builder constructors() {
            constructors = true;
            return this;
        }

        public Builder interfaces() {
            interfaces = true;
            return this;
        }

        public Builder superClassName() {
            superClass = true;
            return this;
        }

        public Builder currentClassName() {
            currentClass = true;
            return this;
        }

        public ParseResultStruct build() {
            CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
            ParseResultStruct parseResultStruct = new ParseResultStruct();
            List typeList = compilationUnit.types();
            if (typeList.size() != 1) {
                return parseResultStruct;
            }
            ASTNode root = compilationUnit.getRoot();
            root.accept(new ASTVisitor() {
                @Override
                public boolean visit(TypeDeclaration typeNote) {
                    if (methods) {
                        parseResultStruct.methods = getMethods(typeNote);
                    }
                    if (fields) {
                        parseResultStruct.fields = getFields(typeNote);
                    }
                    if (constructors) {
                        parseResultStruct.constructors = getConstructors(typeNote);
                    }
                    if (superClass) {
                        parseResultStruct.superClass = parseSuperclassName(typeNote);
                    }
                    if (interfaces) {
                        parseResultStruct.interfaces = parseInterfaces(typeNote);
                    }
                    if (currentClass) {
                        parseResultStruct.currentClass = parseCurrentClass(typeNote);
                    }
                    return false;
                }
            });

            return parseResultStruct;
        }

        private String parseCurrentClass(TypeDeclaration typeNote) {
            return typeNote.getName().getIdentifier();
        }

        private List<String> parseInterfaces(TypeDeclaration typeNote) {
            List<String> interfaceNames = new ArrayList<>();
            typeNote.superInterfaceTypes().forEach(interfaces -> {
                interfaceNames.add(String.valueOf(((SimpleType) interfaces).getName()));
            }); return interfaceNames;
        }

        public static String parseSuperclassName(TypeDeclaration typeNote) {
            Type superclassType = typeNote.getSuperclassType();
            if (superclassType == null) {
                return null;
            }
            return ((SimpleType) superclassType).getName().getFullyQualifiedName();
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


        private static List<MethodSimpleDeclaration> getConstructors(TypeDeclaration typeNote) {
            List<MethodDeclaration> list = new ArrayList<>();
            MethodDeclaration[] notes = typeNote.getMethods();
            for (MethodDeclaration note : notes) {
                if (note.isConstructor()) {
                    note.setBody(null);
                    note.setJavadoc(null);
                    list.add(note);
                }
            }
            return list.stream().map(MethodSimpleDeclaration::new).collect(Collectors.toList());
        }


        private static List<MethodSimpleDeclaration> getMethods(TypeDeclaration typeNote) {
            List<MethodDeclaration> list = new ArrayList<>();
            MethodDeclaration[] notes = typeNote.getMethods();
            for (MethodDeclaration note : notes) {
                if (Modifier.isStatic(note.getModifiers()) || Modifier.isPrivate(note.getModifiers()) ||
                    note.isConstructor()) {
                    continue;
                }
                note.setBody(null);
                note.setJavadoc(null);
                list.add(note);

            }

            return list.stream().map(MethodSimpleDeclaration::new).collect(Collectors.toList());
        }

    }
}