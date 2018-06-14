package com.credits.wallet.desktop.utils;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        String normalizedSourceCode = sourceCode.replace("\r", " ").replace("\n", " ").replace("{", " {");

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

}
