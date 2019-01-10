package com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete;

import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AutocompleteHelper {

    public static final List<String> TYPE_KEYWORDS =
        Arrays.asList("void", "String", "int", "Integer", "boolean", "Boolean", "byte", "Byte", "float", "long", "Long",
            "Float", "double", "Double", "char", "Character", "short", "Short", "enum");

    public static final List<String> LANGUAGE_KEYWORDS =
        Arrays.asList("public", "private", "if", "else", "this", "new", "for", "while", "break", "final", "static",
            "return", "switch", "case", "super", "throws", "throw", "true", "false", "try", "catch", "class", "implements", "extends",
            "protected", "abstract", "assert", "const", "interface", "continue", "default", "do", "finally", "goto",
            "import", "instanceof", "native", "package", "strictfp", "synchronized", "transient", "volatile");

    private CreditsCodeArea codeArea;

    private Map<MethodDeclaration, String> classMethods = new HashMap<>();
    private Map<FieldDeclaration, String> classFields = new HashMap<>();

    private CreditsProposalsPopup creditsProposalsPopup;

    public AutocompleteHelper(CreditsCodeArea creditsCodeArea, CreditsProposalsPopup targetCreditsProposalsPopup) {
        this.codeArea = creditsCodeArea;
        this.creditsProposalsPopup = targetCreditsProposalsPopup;
    }

    private void updateDynamicProposals() {
        Pair<Map<MethodDeclaration, String>, Map<FieldDeclaration, String>> MethodsFieldsPair =
            ParseCodeUtils.getMethodsAndFieldsFromSourceCode(codeArea.getText());
        classMethods = MethodsFieldsPair.getKey();
        classFields = MethodsFieldsPair.getValue();
    }

    public void handleKeyPressEvent(KeyEvent keyEvent) {
        creditsProposalsPopup.clearAndHide();
        boolean isCtrlSpacePressed = (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.SPACE));
        if ((!keyEvent.isAltDown()) &&
            ((keyEvent.getText().trim().length() == 1 && !keyEvent.isControlDown()) || isCtrlSpacePressed)) {

            updateDynamicProposals();
            StringBuilder word = new StringBuilder();
            int pos = codeArea.getCaretPosition();
            String text = codeArea.getText();
            String currentSymbol;
            if (isCtrlSpacePressed) {
                pos--;
                currentSymbol = text.substring(pos, pos + 1);
            } else {
                currentSymbol = keyEvent.getText();
            }
            while (pos > 0 && !currentSymbol.equals(" ") && !currentSymbol.equals("\r") &&
                !currentSymbol.equals("\n") && !currentSymbol.equals("{") && !currentSymbol.equals("(")) {
                word.insert(0, currentSymbol);
                pos--;
                currentSymbol = text.substring(pos, pos + 1);
            }

            String autoCompletedWord = word.toString().trim().toUpperCase();

            if(!autoCompletedWord.isEmpty()) {
                TYPE_KEYWORDS.forEach(keyword -> {
                    if (keyword.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(keyword, keyword, this::handleAutoCompleteWord);
                    }
                });

                LANGUAGE_KEYWORDS.forEach(keyword -> {
                    if (keyword.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(keyword, keyword, this::handleAutoCompleteWord);
                    }
                });

                classFields.forEach((k, v) -> {
                    String fieldName = ((VariableDeclarationFragment) k.fragments().get(0)).getName().toString();
                    if (fieldName.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(fieldName, v, this::handleAutoCompleteWord);
                    }
                });

                CreditsProposalsPopup.parentsFields.forEach((k, fieldName) -> {
                    if (k.getName().toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(k.getName(), fieldName, this::handleAutoCompleteWord);
                    }
                });

                classMethods.forEach((method, v) -> {
                    String methodName = method.getName().getIdentifier();
                    if (methodName.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(method.getName().getIdentifier(), v, this::handleAutoCompleteMethod);
                    }
                });

                CreditsProposalsPopup.parentsMethods.forEach((method, v) -> {
                    String methodName = method.getName();
                    if (methodName.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(method.getName(), v, this::handleAutoCompleteMethod);
                    }
                });
            }

            if (creditsProposalsPopup.isEmpty()) {
                if (isCtrlSpacePressed) {
                    creditsProposalsPopup.addItem(new ProposalItem(null, "No suggestions"));

                }
            } else {
                codeArea.getCaretBounds()
                    .ifPresent(bounds -> creditsProposalsPopup.show(codeArea, bounds.getMaxX(), bounds.getMaxY()));
            }
        }
    }

    private void addProposal(String autoCompleteText, String displayText, Consumer<String> actionHandler) {
        ProposalItem item = new ProposalItem(autoCompleteText, displayText);
        item.setActionHandler(actionHandler);
        creditsProposalsPopup.addItem(item);
    }

    private void handleAutoCompleteMethod(String methodName) {
        String textToInsert = methodName + "()";
        codeArea.doAutoComplete(textToInsert);
        codeArea.selectRange(this.codeArea.getCaretPosition() - 1, this.codeArea.getCaretPosition() - 1);
    }

    private void handleAutoCompleteWord(String keyword) {
        codeArea.doAutoComplete(keyword);
    }

}
