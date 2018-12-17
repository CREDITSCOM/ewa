package com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete;

import com.credits.wallet.desktop.utils.sourcecode.ParseSourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.HashMap;
import java.util.Map;

public class AutocompleteHelper {


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
            ParseSourceCodeUtils.getMethodsAndFieldsFromSourceCode(codeArea.getText());
        classMethods = MethodsFieldsPair.getKey();
        classFields = MethodsFieldsPair.getValue();
    }

    public void handleKeyPressEvent(KeyEvent keyEvent) {
        creditsProposalsPopup.clearAndHide();
        boolean isCtrlSpacePressed = (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.SPACE));
        if ((!keyEvent.isShiftDown() && !keyEvent.isAltDown()) &&
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

            String finalWord = word.toString();

            CreditsProposalsPopup.javaKeywords.forEach(keyword -> {
                if (finalWord.trim().isEmpty() || keyword.toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(keyword, keyword);
                    item.setActionHandler(actionHandler -> handleActionJavaKeywords(keyword));
                    creditsProposalsPopup.addItem(item);
                }
            });

            classFields.forEach((k, v) -> {
                String fieldName = ((VariableDeclarationFragment) k.fragments().get(0)).getName().toString();
                if (finalWord.trim().isEmpty() || fieldName.toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(fieldName, v);
                    item.setActionHandler(actionHandler -> handleActionFields(fieldName));
                    creditsProposalsPopup.addItem(item);
                }
            });

            CreditsProposalsPopup.parentsFields.forEach((k, fieldName) -> {
                if (finalWord.trim().isEmpty() || k.getName().toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(fieldName, fieldName);
                    item.setActionHandler(actionHandler -> handleActionFields(fieldName));
                    creditsProposalsPopup.addItem(item);
                }
            });

            classMethods.forEach((method, v) -> {
                if (finalWord.trim().isEmpty() ||
                    method.getName().getIdentifier().toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(method.getName().getIdentifier(), v);
                    item.setActionHandler(actionHandler -> handleActionMethods(method.getName().getIdentifier()));
                    creditsProposalsPopup.addItem(item);
                }
            });

            CreditsProposalsPopup.parentsMethods.forEach((method, v) -> {
                if (finalWord.trim().isEmpty() || method.getName().toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(method.getName(), v);
                    item.setActionHandler(actionHandler -> handleActionMethods(method.getName()));
                    creditsProposalsPopup.addItem(item);
                }
            });

            if (creditsProposalsPopup.isEmpty() && isCtrlSpacePressed) {
                creditsProposalsPopup.addItem(new ProposalItem(null, "No suggestions"));

            }

            if (!creditsProposalsPopup.isEmpty()) {
                creditsProposalsPopup.show(codeArea, codeArea.getCaretBounds().get().getMaxX(),
                    codeArea.getCaretBounds().get().getMaxY());
            }
        }
    }

    private void handleActionFields(String fieldName) {
        codeArea.doAutoComplete(fieldName);
    }

    private void handleActionMethods(String methodName) {
        String textToInsert = methodName + "()";
        codeArea.doAutoComplete(textToInsert);
        codeArea.selectRange(this.codeArea.getCaretPosition() - 1, this.codeArea.getCaretPosition() - 1);
    }

    private void handleActionJavaKeywords(String keyword) {
        codeArea.doAutoComplete(keyword);
    }


}
