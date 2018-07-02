package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceCodeUtils {
    private static final String[] KEYWORDS =
        new String[] {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"};

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[]]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
        "(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN +
            ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" + "|(?<STRING>" +
            STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")");


    public static String normalizeSourceCode(String sourceCode) {
        String normalizedSourceCode =
            sourceCode.replace("\r", " ").replace("\n", " ").replace("\t", " ").replace("{", " {");

        while (normalizedSourceCode.contains("  ")) {
            normalizedSourceCode = normalizedSourceCode.replace("  ", " ");
        }
        return normalizedSourceCode;
    }

    public static String parseClassName(String sourceCode, String defaultClassName) {
        String normalizedSourceCode = normalizeSourceCode(sourceCode);
        String className = defaultClassName;
        List<String> javaCodeWords = Arrays.asList(normalizedSourceCode.split(" "));
        int ind = javaCodeWords.indexOf("class");
        if (ind >= 0 && ind < javaCodeWords.size() - 1) {
            className = javaCodeWords.get(ind + 1);
        }
        return className;
    }

    public static String normalizeMethodName(String methodSignature) {
        int ind1 = methodSignature.indexOf(" ");
        String result = methodSignature.substring(ind1 + 1);

        ind1 = result.indexOf("(");
        int ind2 = result.indexOf(")");
        StringBuilder parametersStr = new StringBuilder();
        String[] parameters = result.substring(ind1, ind2).trim().split(",");
        boolean first = true;
        for (String parameter : parameters) {
            String[] parameterAsArr = parameter.trim().split(" ");
            if (first) {
                parametersStr.append(parameterAsArr[1].trim());
            } else {
                parametersStr.append(", ").append(parameterAsArr[1].trim());
            }
            first = false;
        }

        return result.substring(0, ind1 + 1) + parametersStr + ")";
    }

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" : matcher.group("PAREN") != null ? "paren"
                : matcher.group("BRACE") != null ? "brace" : matcher.group("BRACKET") != null ? "bracket"
                    : matcher.group("SEMICOLON") != null ? "semicolon" : matcher.group("STRING") != null ? "string"
                        : matcher.group("COMMENT") != null ? "comment" : null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public static CodeArea initCodeArea(Pane paneCode) {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        CodeArea codeArea = new CodeArea();
        codeArea.setPrefHeight(paneCode.getPrefHeight());
        codeArea.setPrefWidth(paneCode.getPrefWidth());
        paneCode.getChildren().add(codeArea);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
            .successionEnds(Duration.ofMillis(500)).supplyTask(() -> {
            String sourceCode = codeArea.getText();
            Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
                @Override
                protected StyleSpans<Collection<String>> call() {
                    return SourceCodeUtils.computeHighlighting(sourceCode);
                }
            };
            AppState.executor.execute(task);
            return task;
        }).awaitLatest(codeArea.richChanges()).filterMap(t -> {
            if (t.isSuccess()) {
                return Optional.of(t.get());
            } else {
                t.getFailure().printStackTrace();
                return Optional.empty();
            }
        }).subscribe(highlighting -> codeArea.setStyleSpans(0, highlighting));

        return codeArea;
    }

    public static String parseClassName(String sourceCode) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);

        List typeList = compilationUnit.types();

        if (typeList.size() != 1) {
            return null;
        }

        TypeDeclaration typeDeclaration = (TypeDeclaration) typeList.get(0);

        return (typeDeclaration).getName().getFullyQualifiedName();
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
                FieldDeclaration[] notes = typeNote.getFields();
                list.addAll(Arrays.asList(notes));
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
                MethodDeclaration[] notes = typeNote.getMethods();
                for (MethodDeclaration note : notes) {
                    if (!note.isConstructor()) {
                        list.add(note);
                    }
                }
                return false;
            }
        });
        return list;
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
                MethodDeclaration[] notes = typeNote.getMethods();
                for (MethodDeclaration note : notes) {
                    if (note.isConstructor()) {
                        list.add(note);
                    }
                }
                return false;
            }
        });
        return list;
    }

    public static List<SingleVariableDeclaration> getMethodParameters(MethodDeclaration methodDeclaration) {
        return methodDeclaration.parameters();
    }

    public static int getLineNumber(String sourceCode, BodyDeclaration bodyDeclaration) {
        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);
        return compilationUnit.getLineNumber(bodyDeclaration.getStartPosition());
    }

    public static String generateSmartContractToken() {
        return "CST" + com.credits.common.utils.Utils.randomAlphaNumeric(29);
    }

    public static String formatSourceCode(String source) {
        // take default Eclipse formatting options
        Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

        // initialize the compiler settings to be able to format 1.8 code
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);

        // change the option to wrap each enum constant on a new line
        options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
            DefaultCodeFormatterConstants.createAlignmentValue(true, DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
                DefaultCodeFormatterConstants.INDENT_ON_COLUMN));

        // instantiate the default code formatter with the given options
        final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

        final TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, // format a compilation unit
            source, // source to format
            0, // starting position
            source.length(), // length
            0, // initial indentation
            System.getProperty("line.separator") // line separator
        );

        IDocument document = new Document(source);
        try {
            edit.apply(document);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // display the formatted string on the System out
        return document.get();
    }
}
