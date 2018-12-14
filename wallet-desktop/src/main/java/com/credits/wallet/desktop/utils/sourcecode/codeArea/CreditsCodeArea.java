package com.credits.wallet.desktop.utils.sourcecode.codeArea;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.AutocompleteHelper;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup;
import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils.computeHighlighting;
import static javafx.scene.input.KeyCode.INSERT;
import static javafx.scene.input.KeyCode.PASTE;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.anyOf;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

public class CreditsCodeArea extends CodeArea {


    private static int tabCount;

    private static final String DEFAULT_SOURCE_CODE =
        "public class Contract extends SmartContract {\n" + "\n" + "    public Contract() {\n\n    }" + "\n" + "}";
    private ExecutorService codeAreaHighlightExecutor = Executors.newSingleThreadExecutor();

    public static final String SPACE_SYMBOL = " ";
    public static final String CURLY_BRACKET_SYMBOL = "{";
    public static final String ROUND_BRACKET_SYMBOL = "(";
    public static final String NEW_LINE_SYMBOL = "\n";


    public CreditsToolboxPopup popup;
    public AutocompleteHelper autocompleteHelper;
    public CreditsProposalsPopup creditsProposalsPopup;

    public CreditsCodeArea(boolean readOnly, double prefHeight, double prefWidth) {
        super();
        this.setPrefHeight(prefHeight);
        this.setPrefWidth(prefWidth);
        this.initCodeAreaLogic();
        popup = new CreditsToolboxPopup(this, readOnly);
        creditsProposalsPopup = new CreditsProposalsPopup();
        autocompleteHelper = new AutocompleteHelper(this, creditsProposalsPopup);
    }

    public void initCodeAreaLogic() {

        this.sceneProperty().addListener((observable, old, newPropertyValue) -> {
            if (newPropertyValue == null) {
                this.cleanAll();
            }
        });

        initKeyPressedLogic();
        initRichTextLogic();
        fillDefaultCodeSource();
    }

    private void initRichTextLogic() {
        this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        this.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
            .successionEnds(Duration.ofMillis(500)).supplyTask(() -> {
            String sourceCode = this.getText();
            Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
                @Override
                protected StyleSpans<Collection<String>> call() {
                    return computeHighlighting(sourceCode);
                }
            };
            codeAreaHighlightExecutor.execute(task);
            return task;
        }).awaitLatest(this.richChanges()).filterMap(t -> {
            if (t.isSuccess()) {
                return Optional.of(t.get());
            } else {
                t.getFailure().printStackTrace();
                return Optional.empty();
            }
        }).subscribe(highlighting -> this.setStyleSpans(0, highlighting));
    }

    private void initKeyPressedLogic() {

        this.setOnMousePressed(event -> {
            if (creditsProposalsPopup.isShowing()) {
                creditsProposalsPopup.hide();
            }
        });

        this.addEventHandler(KeyEvent.KEY_PRESSED, (k) -> {
            KeyCode code = k.getCode();
            if (code != KeyCode.TAB) {
                if (code.isLetterKey() || code.isDigitKey() || code.isNavigationKey() || code.isWhitespaceKey()) {
                    tabCount = 0;
                }
            }
            this.autocompleteHelper.handleKeyPressEvent(k);
        });

        Nodes.addInputMap(this, InputMap.consume(keyPressed(KeyCode.TAB), e -> {
            tabCount++;
            this.replaceSelection(StringUtils.repeat(" ", 4));
        }));

        Nodes.addInputMap(this,
            InputMap.consume(anyOf(keyPressed(PASTE), keyPressed(V, SHORTCUT_DOWN), keyPressed(INSERT, SHIFT_DOWN)),
                e -> {
                    replaceTabSymbolInClipboard();
                }));

        Nodes.addInputMap(this, InputMap.consume(keyPressed(KeyCode.ENTER), e -> {
            try {
                calculateNewLinePosition();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                this.replaceSelection("\n");
            }
        }));

        Nodes.addInputMap(this,
            InputMap.consumeWhen(keyPressed(KeyCode.BACK_SPACE), () -> this.getSelectedText().equals(""), e -> {
                if (tabCount > 0) {
                    for (int i = 0; i < 4; i++) {
                        this.deletePreviousChar();
                    }
                    tabCount--;
                } else {
                    this.deletePreviousChar();
                }
            }));
    }

    public void replaceTabSymbolInClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String oldStringFromClipboard = clipboard.getString();
            ClipboardContent content = new ClipboardContent();
            content.putString(oldStringFromClipboard.replaceAll("\t", StringUtils.repeat(" ", 4)));
            clipboard.setContent(content);
            this.paste();
            content.putString(oldStringFromClipboard);
            clipboard.setContent(content);
        }
    }


    private void calculateNewLinePosition() {
        String currentLine = getCurentLine(this);
        int first = getPositionFirstNotSpecialCharacter(currentLine);

        String replacement = "\n" + StringUtils.repeat(" ", first);
        String trimCurrentLine = currentLine.trim();
        char c = trimCurrentLine.charAt(trimCurrentLine.length() - 1);
        if (c == '{') {
            tabCount++;
            replacement += StringUtils.repeat(" ", 4);
        }
        this.replaceSelection(replacement);
    }

    public int getPositionFirstNotSpecialCharacter(String currentLine) {
        Matcher matcher = Pattern.compile("[^ ^\t]").matcher(currentLine);
        matcher.find();
        return matcher.start();
    }

    public String getCurentLine(CreditsCodeArea creditsCodeArea) {
        String substring = this.getText().substring(0, creditsCodeArea.getCaretPosition());
        /*if(substring.charAt(substring.length()-1) == '\n') {
            substring = substring.substring(0,substring.length()-2);
        }*/
        int lastIndexOfNewLine = substring.lastIndexOf('\n');
        return substring.substring(lastIndexOfNewLine + 1, creditsCodeArea.getCaretPosition());
    }

    public void doAutoComplete(String textToInsert) {
        String token = "%";
        String text = this.getText().replace(token, "?");
        int caretPos = this.getCaretPosition();
        int lastIndexOfSpaceSymbol = text.lastIndexOf(SPACE_SYMBOL, caretPos - 1);
        int lastIndexOfCurlyBracketSymbol = text.lastIndexOf(CURLY_BRACKET_SYMBOL, caretPos - 1);
        int lastIndexOfRoundBracketSymbol = text.lastIndexOf(ROUND_BRACKET_SYMBOL, caretPos - 1);
        int lastIndexOfNewLineSymbol = text.lastIndexOf(NEW_LINE_SYMBOL, caretPos - 1);
        StringBuilder b = new StringBuilder(text);
        if (lastIndexOfSpaceSymbol != -1) {
            b.replace(lastIndexOfSpaceSymbol, lastIndexOfSpaceSymbol + 1, token);
        }
        if (lastIndexOfCurlyBracketSymbol != -1) {
            b.replace(lastIndexOfCurlyBracketSymbol, lastIndexOfCurlyBracketSymbol + 1, token);
        }
        if (lastIndexOfRoundBracketSymbol != -1) {
            b.replace(lastIndexOfRoundBracketSymbol, lastIndexOfRoundBracketSymbol + 1, token);
        }
        if (lastIndexOfNewLineSymbol != -1) {
            b.replace(lastIndexOfNewLineSymbol, lastIndexOfNewLineSymbol + 1, token);
        }
        String textReplacedSymbols = b.toString();
        int spacePos = textReplacedSymbols.lastIndexOf(token, caretPos);
        this.replaceText(spacePos + 1, caretPos, textToInsert);
    }

    public void positionCursorToLine(int line) {
        char[] text = this.getText().toCharArray();
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
        this.displaceCaret(pos);
        this.showParagraphAtTop(Math.max(0, line - 5));
        this.requestFocus();
    }

    public void fillDefaultCodeSource() {
        if (AppState.lastSmartContract == null) {
            this.replaceText(0, 0, DEFAULT_SOURCE_CODE);
        } else {
            this.replaceText(AppState.lastSmartContract);
        }
    }

    public void cleanAll() {
        popup.hide();
        creditsProposalsPopup.clear();
        creditsProposalsPopup.hide();
        codeAreaHighlightExecutor.shutdown();
    }

}
