package com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete;

import com.credits.scapi.v0.SmartContract;
import com.credits.wallet.desktop.struct.MethodSimpleDeclaration;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.*;
import java.util.function.Consumer;

import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.*;

public class AutocompleteHelper {

    private static final List<String> TYPE_KEYWORDS =
        Arrays.asList("void", "String", "int", "Integer", "boolean", "Boolean", "byte", "Byte", "float", "long", "Long",
                      "Float", "double", "Double", "char", "Character", "short", "Short", "enum");

    private static final List<String> LANGUAGE_KEYWORDS =
        Arrays.asList("public", "private", "if", "else", "this", "new", "for", "while", "break", "final", "static",
                      "return", "switch", "case", "super", "throws", "throw", "true", "false", "try", "catch", "class",
                      "implements", "extends", "protected", "abstract", "assert", "const", "interface", "continue", "default",
                      "do", "finally", "goto", "import", "instanceof", "native", "package", "strictfp", "synchronized",
                      "transient", "volatile");

    private static final List<String> INTERFACES_KEYWORDS =
        Arrays.asList(BASIC_STANDARD_CLASS, EXTENSION_STANDARD_CLASS);
    private CreditsCodeArea codeArea;

    private Map<MethodSimpleDeclaration, String> classMethods = new HashMap<>();
    private Map<FieldDeclaration, String> classFields = new HashMap<>();
    private List<String> interfaces;
    String superClassName;

    private CreditsProposalsPopup creditsProposalsPopup;

    public AutocompleteHelper(CreditsCodeArea creditsCodeArea, CreditsProposalsPopup targetCreditsProposalsPopup) {
        this.codeArea = creditsCodeArea;
        this.creditsProposalsPopup = targetCreditsProposalsPopup;
    }

    private void updateDynamicProposals() {
        ParseResultStruct build =
            new ParseResultStruct.Builder(codeArea.getText()).fields().superClassName().interfaces().methods().build();
        superClassName = SmartContract.class.getName();
        interfaces = new ArrayList<>();
        interfaces.addAll(build.interfaces);
        classMethods.clear();
        build.methods.forEach(method -> classMethods.put(method, method.toString()));
        classFields.clear();
        build.fields.forEach(field -> classFields.put(field, field.toString()));
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

            if (!autoCompletedWord.isEmpty()) {

                INTERFACES_KEYWORDS.forEach(curInterface -> {
                    final String[] partsName = curInterface.split("\\.");
                    String interfaceName = partsName[partsName.length - 1];
                    if (interfaceName.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(interfaceName, interfaceName, this::handleAutoCompleteWord);
                    }
                });

                TYPE_KEYWORDS.forEach(keyword -> {
                    if (keyword.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(keyword, keyword, this::handleAutoCompleteWord);
                    }
                });


                try {
                    for (String anInterface : interfaces) {
                        if (anInterface.equals(BASIC_STANDARD_CLASS)) {
                            getFieldsAndMethodsFromSourceCode(BASIC_STANDARD_CLASS);
                        } else if (anInterface.equals(CreditsProposalsPopup.EXTENSION_STANDARD_CLASS)) {
                            getFieldsAndMethodsFromSourceCode(BASIC_STANDARD_CLASS);
                            getFieldsAndMethodsFromSourceCode(CreditsProposalsPopup.EXTENSION_STANDARD_CLASS);
                        }
                    }
                    getFieldsAndMethodsFromSourceCode(superClassName);
                } catch (Exception ignored) {
                }


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
                    String methodName = method.getMethodDeclaration().getName().getIdentifier();
                    if (methodName.toUpperCase().startsWith(autoCompletedWord)) {
                        addProposal(method.getMethodDeclaration().getName().getIdentifier(), v, this::handleAutoCompleteMethod);
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
