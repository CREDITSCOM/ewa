package com.credits.general.util;

import java.util.Arrays;
import java.util.List;

public class GeneralSourceCodeUtils {
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

    public static String normalizeSourceCode(String sourceCode) {
        String normalizedSourceCode =
            sourceCode.replace("\r", " ").replace("\t", " ").replace("{", " {");

        while (normalizedSourceCode.contains("  ")) {
            normalizedSourceCode = normalizedSourceCode.replace("  ", " ");
        }
        return normalizedSourceCode;
    }


}
