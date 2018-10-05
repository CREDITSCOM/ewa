package com.credits.wallet.desktop.utils;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.ApiTransactionThreadRunnable;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.util.ApiAlertUtils;
import com.credits.wallet.desktop.AppState;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartContractUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(SmartContractUtils.class);

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
                    return SmartContractUtils.computeHighlighting(sourceCode);
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

    public static BigDecimal getSmartContractBalance(String smart) {
        try {
            String owner = "\"" + AppState.account + "\"";
            String method = "balanceOf";
            List<String> params = new ArrayList<>();
            params.add(owner);
            SmartContractData smartContractData =
                AppState.levelDbService.getSmartContract(Converter.decodeFromBASE58(smart));
            if (smartContractData == null) {
                FormUtils.showInfo("SmartContract not found");
                return null;
            }
            smartContractData.setMethod(method);
            smartContractData.setParams(params);
            return new BigDecimal(AppState.levelDbService.directExecuteSmartContract(smartContractData)
                .getRet_val()
                .getV_double()).setScale(13, BigDecimal.ROUND_DOWN);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
        return null;
    }

    public static void transferTo(String smart, String target, BigDecimal amount) {
        try {
            String method = "transfer";
            List<Object> params = new ArrayList<>();
            params.add("\"" + target + "\"");
            params.add(amount);
            SmartContractData smartContractData =
                AppState.levelDbService.getSmartContract(Converter.decodeFromBASE58(smart));
            if (smartContractData == null) {
                FormUtils.showInfo("SmartContract not found");
                return;
            }
            ApiUtils.executeSmartContractProcess(method, params, smartContractData, new ApiTransactionThreadRunnable.Callback() {
                @Override
                public void onSuccess(ApiResponseData resultData) {
                    ApiAlertUtils.showInfo("Transfer is ok");
                }

                @Override
                public void onError(Exception e) {
                    ApiAlertUtils.showError(e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
    }
}
