package com.credits.wallet.desktop.utils.sourcecode;


import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class SourceCodeUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(SourceCodeUtils.class);

    // TODO unused, candidate to removing
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
            source, // account to format
            0, // starting position
            source.length(), // length
            0, // initial indentation
            System.getProperty("line.separator") // line separator
        );

        IDocument document = new Document(source);
        try {
            edit.apply(document);
        } catch (BadLocationException e) {
            LOGGER.error("failed!", e );
        }

        // display the formatted string on the System out
        return document.get();
    }
}
