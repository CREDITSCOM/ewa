package com.credits.wallet.desktop.utils.sourcecode.codeArea;

import com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.AutocompleteHelper;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup;
import javafx.concurrent.Task;
import javafx.scene.control.IndexRange;
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
import static javafx.scene.input.KeyCode.SHIFT;
import static javafx.scene.input.KeyCode.V;
import static javafx.scene.input.KeyCode.Y;
import static javafx.scene.input.KeyCode.Z;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.anyOf;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

public class CreditsCodeArea extends CodeArea {

    private static final int TAB_SIZE = 4;
    private static final String SPACE_SYMBOL = " ";
    private static final String CURLY_BRACKET_SYMBOL = "{";
    private static final String ROUND_BRACKET_SYMBOL = "(";
    private static final String NEW_LINE_SYMBOL = "\n";
    private static final String TAB_STRING = StringUtils.repeat(" ", TAB_SIZE);

    private static int tabCount;
    private ExecutorService codeAreaHighlightExecutor = Executors.newSingleThreadExecutor();

    private long lastTimeStampOfSavedText;
    private CreditsToolboxPopup popup;
    private AutocompleteHelper autocompleteHelper;
    private CreditsProposalsPopup creditsProposalsPopup;

    CreditsCodeArea(boolean readOnly, double prefHeight, double prefWidth) {
        super();
        this.setPrefHeight(prefHeight);
        this.setPrefWidth(prefWidth);
        this.initCodeAreaLogic();
        popup = new CreditsToolboxPopup(this, readOnly);
        creditsProposalsPopup = new CreditsProposalsPopup();
        autocompleteHelper = new AutocompleteHelper(this, creditsProposalsPopup);
    }

    private void initCodeAreaLogic() {
        initKeyPressedLogic();
        initRichTextLogic();
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
            if (code != SHIFT) {
                if (code != KeyCode.TAB) {
                    if (code.isLetterKey() || code.isDigitKey() || code.isNavigationKey() || code.isWhitespaceKey()) {
                        tabCount = 0;
                    }
                }
                this.autocompleteHelper.handleKeyPressEvent(k);
            }
        });


        Nodes.addInputMap(this, InputMap.consume(keyPressed(Z, SHORTCUT_DOWN), e -> {
            this.undo();
            this.fixCaretPosition(this.getCaretPosition());
        }));


        Nodes.addInputMap(this, InputMap.consume(keyPressed(Y, SHORTCUT_DOWN), e -> {
            this.redo();
            this.fixCaretPosition(this.getCaretPosition());
        }));

        Nodes.addInputMap(this, InputMap.consume(keyPressed(KeyCode.TAB), e -> {
            tabCount++;
            this.replaceSelection(TAB_STRING);
        }));

        Nodes.addInputMap(this, InputMap.consume(keyPressed(KeyCode.ENTER), e -> {
            String currentLine = getText().split("\n")[getCaretPositionOnLines().lineNumber];
            tabCount = countTabsNumberAtBeginLine(currentLine);
            int caretPosition = getCaretPositionOnLines().position;
            if (currentLine.length() > 0) {
                if (getCharBeforeCaret(currentLine, caretPosition) == '{' && isBraceRequired()) {
                    tabCount += 1;
                    replaceSelection("\n\n" + StringUtils.repeat(TAB_STRING, tabCount - 1) + "}");
                    setCaretPositionOnLine(getCaretPositionOnLines().lineNumber);
                    replaceSelection(StringUtils.repeat(TAB_STRING, tabCount));
                } else {
                    if (getCharBeforeCaret(currentLine, caretPosition) == '{') {
                        tabCount += 1;
                    }
                    replaceSelection("\n" + StringUtils.repeat(TAB_STRING, tabCount));
                }
            } else {
                replaceSelection("\n");
            }
        }));

        Nodes.addInputMap(this,
                InputMap.consume(anyOf(keyPressed(PASTE), keyPressed(V, SHORTCUT_DOWN), keyPressed(INSERT, SHIFT_DOWN)),
                        e -> replaceTabSymbolInClipboard()));

        Nodes.addInputMap(this, InputMap.consume(keyPressed(KeyCode.BACK_SPACE), e -> {
            if (tabCount > 0) {
                for (int i = 0; i < TAB_SIZE; i++) {
                    deletePreviousChar();
                }
                tabCount--;
            } else {
                IndexRange selectedTextRange = getSelection();
                if(selectedTextRange.getLength() > 0){
                    deleteText(selectedTextRange);
                }else {
                    deletePreviousChar();
                }
            }
        }));
    }

    private char getCharBeforeCaret(String currentLine, int caretPosition) {
        return currentLine.charAt(caretPosition > 0 ? caretPosition - 1 : 0);
    }


    private boolean isBraceRequired() {
        int[] chars = getText().chars().toArray();
        int unclosedBraces = 0;
        for (int aChar : chars) {
            if (aChar == '{') {
                unclosedBraces++;
            } else if (aChar == '}') {
                unclosedBraces--;
            }
        }
        return unclosedBraces > 0;
    }

    private int countTabsNumberAtBeginLine(String currentLine) {
        StringBuilder sb = new StringBuilder(currentLine);
        for (int i = 0; i < sb.length(); i++) {
            if(sb.charAt(i) == '\t') {
                i += TAB_SIZE - 1;
                continue;
            }
            if (sb.charAt(i) != ' ') {
                return (i + 1) / TAB_SIZE;
            }
        }
        return sb.length() / TAB_SIZE;
    }

    void replaceTabSymbolInClipboard() {
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


    public int getPositionFirstNotSpecialCharacter(String currentLine) {
        try {
            Matcher matcher = Pattern.compile("[^ ^\t]").matcher(currentLine);
            matcher.find();
            return matcher.start();
        } catch (Exception e) {
            return 0;
        }
    }

    public CaretLinePosition getCaretPositionOnLines() {
        int caretPosition = this.getCaretPosition();
        int amountCharsBeforeCaretInLine = 0;
        int i = 0;
        String[] lines = this.getText().split("\n");
        for (String line : lines) {
            amountCharsBeforeCaretInLine += line.length();
            if (caretPosition <= amountCharsBeforeCaretInLine) {
                return new CaretLinePosition(i, lines, i == 0 ? caretPosition : line.length() - (amountCharsBeforeCaretInLine - caretPosition));
            }
            amountCharsBeforeCaretInLine = amountCharsBeforeCaretInLine + 1;
            i++;
        }
        return new CaretLinePosition(0, lines, lines.length > 0 ? lines[lines.length - 1].length() : 0);
    }

    public void setCaretPositionOnLine(int lineNumber) {
        this.positionCursorToLine(lineNumber);
        CreditsCodeArea.CaretLinePosition caretLinePosition = getCaretPositionOnLines();
        String currentLine = caretLinePosition.lines[caretLinePosition.lineNumber];
        this.fixCaretPosition(this.getCaretPosition() + this.getPositionFirstNotSpecialCharacter(
            currentLine));
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

    public void fixCaretPosition(int caretPosition) {
        this.selectRange(caretPosition,caretPosition);
    }

    public void cleanAll() {
        popup.hide();
        creditsProposalsPopup.clear();
        creditsProposalsPopup.hide();
        codeAreaHighlightExecutor.shutdown();
    }

    public class CaretLinePosition {
        public int lineNumber;
        public String[] lines;
        public int position;

        public CaretLinePosition(int lineNumber, String[] lines, int position) {
            this.lineNumber = lineNumber;
            this.lines = lines;
            this.position = position;
        }
    }

}
