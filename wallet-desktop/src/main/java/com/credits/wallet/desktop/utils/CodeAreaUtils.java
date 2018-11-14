package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.sourcecode.AutocompleteHelper;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

public class CodeAreaUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(CodeAreaUtils.class);

    private static final String DEFAULT_SOURCE_CODE =
        "public class Contract extends SmartContract {\n" + "\n" + "    public Contract() {\n\n    }" + "\n" + "}";
    private static final String[] PARENT_METHODS =
        new String[] {"double total", "Double getBalance(String address, String currency)",
            "TransactionData getTransaction(String transactionId)",
            "List<TransactionData> getTransactions(String address, long offset, long limit)",
            "List<PoolData> getPoolList(long offset, long limit)", "PoolData getPool(String poolNumber)",
            "void sendTransaction(String account, String target, Double amount, String currency)"};

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



    private static AutocompleteHelper autocompleteHelper;
    private static int tabCount;

    public static void initCodeAreaLogic(CodeArea codeArea) {

        try {
            autocompleteHelper = AutocompleteHelper.init(codeArea);
        } catch (WalletDesktopException e) {
            LOGGER.error("", e);
            FormUtils.showError(e.getMessage());
        }

        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (k) -> {
            KeyCode code = k.getCode();
            if (code != KeyCode.TAB) {
                if (code.isLetterKey() || code.isDigitKey() || code.isNavigationKey() || code.isWhitespaceKey()) {
                    tabCount = 0;
                }
            }

            autocompleteHelper.handleKeyPressEvent(k);
        });

        Nodes.addInputMap(codeArea, InputMap.consume(keyPressed(KeyCode.TAB), e -> {
            tabCount++;
            codeArea.replaceSelection("    ");
        }));

        Nodes.addInputMap(codeArea, InputMap.consume(keyPressed(KeyCode.BACK_SPACE), e -> {
            if (tabCount > 0) {
                for (int i = 0; i < 4; i++) {
                    codeArea.deletePreviousChar();
                }
                tabCount--;
            } else {
                codeArea.deletePreviousChar();
            }
        }));

        if(AppState.lastSmartContract == null) {
            codeArea.replaceText(0, 0, DEFAULT_SOURCE_CODE);
        } else {
            codeArea.replaceText(AppState.lastSmartContract);
        }
    }

    public static CodeArea initCodeArea(Pane paneCode, boolean readOnly) {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        CodeArea codeArea = new CodeArea();
        codeArea.setPrefHeight(paneCode.getPrefHeight());
        codeArea.setPrefWidth(paneCode.getPrefWidth());
        paneCode.getChildren().add(new VirtualizedScrollPane<>(codeArea));
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
            .successionEnds(Duration.ofMillis(500)).supplyTask(() -> {
            String sourceCode = codeArea.getText();
            Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
                @Override
                protected StyleSpans<Collection<String>> call() {
                    return computeHighlighting(sourceCode);
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

        initCodeAreaContextMenu(codeArea, readOnly);

        return codeArea;
    }

    public static void positionCursorToLine(CodeArea codeArea, int line) {
        char[] text = codeArea.getText().toCharArray();
        int pos = 0;
        int curLine = 1;
        while (pos < text.length) {
            if (line <= curLine) {
                break;
            }
            if (text[pos] == '\n') {
                curLine++;
            }
            pos++;
        }
        codeArea.displaceCaret(pos);
        codeArea.showParagraphAtTop(Math.max(0, line - 5));
        codeArea.requestFocus();
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
            spansBuilder.add(emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public static void initCodeAreaContextMenu(CodeArea codeArea, boolean readOnly) {

        Popup popup = new Popup();
        Button cut = new Button("Cut");
        cut.setDisable(readOnly);
        cut.setOnAction(e -> {
            codeArea.cut();
            popup.hide();
        });
        Button copy = new Button("Copy");
        copy.setOnAction(e -> {
            codeArea.copy();
            popup.hide();
        });
        Button paste = new Button("Paste");
        paste.setDisable(readOnly);
        paste.setOnAction(e -> {
            codeArea.paste();
            popup.hide();
        });
        Button select = new Button("Select All");
        select.setOnAction(e -> {
            codeArea.selectAll();
            popup.hide();
        });
        VBox box = new VBox();
        box.setId("popup");
        box.getStylesheets().add(App.class.getResource("/context-menu.css").toExternalForm());
        box.setPrefSize(80, 50);
        box.getChildren().addAll(cut, copy, paste, select);

        popup.getContent().add(box);

        EventHandler contextMenu = (EventHandler<MouseEvent>) event -> {
            if (event.getButton() == event.getButton().SECONDARY) {
                popup.setAutoFix(false);
                popup.show(codeArea, event.getScreenX(), event.getScreenY());
            } else if (popup.isShowing() && event.getClickCount() == 1) {
                popup.hide();
            }
        };

        codeArea.setOnMouseClicked(contextMenu);
    }














}
