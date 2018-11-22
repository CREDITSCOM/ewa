package com.credits.wallet.desktop.utils.sourcecode.autocomplete;

import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.utils.sourcecode.JavaReflect;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.fxmisc.richtext.CodeArea;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutocompleteHelper {

    private static final List<String> javaKeywords = Arrays.asList("abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
        "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
        "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
        "String", "Boolean", "Integer", "Float", "Byte", "Short", "Long", "Character", "Double");

    private CodeArea targetCodeArea;

    private Map<Method, String> parentsMethods = new HashMap<>();

    private Map<MethodDeclaration, String> classMethods = new HashMap<>();

    private Map<Field, String> parentsFields = new HashMap<>();

    private Map<FieldDeclaration, String> classFields = new HashMap<>();

    private ProposalsMenu proposalsMenu = new ProposalsMenu();

    private AutocompleteHelper() {
    }

    public static AutocompleteHelper init(CodeArea targetCodeArea) throws WalletDesktopException {

        AutocompleteHelper autocompleteHelper = new AutocompleteHelper();

        autocompleteHelper.targetCodeArea = targetCodeArea;
        try {
            autocompleteHelper.updateParentsMetadata("SmartContract");
            autocompleteHelper.updateParentsMetadata("BasicStandard");
            autocompleteHelper.updateParentsMetadata("ExtensionStandard");
        } catch (ClassNotFoundException e) {
            throw new WalletDesktopException(e);
        }
        return autocompleteHelper;
    }

    // ===============
    private static Map<Method, String> mergeMethodMap(Map<Method, String> firstMap, Map<Method, String> secondMap) {
        return Stream.concat(
                firstMap.entrySet().stream(),
                secondMap.entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue
        ));
    }

    private static Map<Field, String> mergeFieldMap(Map<Field, String> firstMap, Map<Field, String> secondMap) {
        return Stream.concat(
                firstMap.entrySet().stream(),
                secondMap.entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue
        ));
    }
    // ===============
    private void updateClassMetadata(String classSource) {

        List<FieldDeclaration> fields = SourceCodeUtils.parseFields(classSource);
        fields.forEach(field -> this.classFields.put(field, field.toString()));

        List<MethodDeclaration> methods = SourceCodeUtils.parseMethods(classSource);
        methods.forEach(method -> {
            method.setBody(null);
            this.classMethods.put(method, method.toString());
        });
    }

    private void updateParentsMetadata(String parentClass) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(parentClass);
        Map<Field, String> declaredFields = JavaReflect.getDeclaredFields(clazz);
        parentsFields = mergeFieldMap(parentsFields, declaredFields);
        Map<Method, String> declaredMethods = JavaReflect.getDeclaredMethods(clazz);
        parentsMethods = mergeMethodMap(parentsMethods, declaredMethods);
    }
    // ===============
    private void classMetadataInit() {
        classFields.clear();
        classMethods.clear();
        updateClassMetadata(targetCodeArea.getText());
    }
    // ===============
    private void handleActionFields(String fieldName) {
        this.doAutoComplete(fieldName);
    }

    private void handleActionMethods(String methodName) {
        String textToInsert = methodName + "()";
        this.doAutoComplete(textToInsert);
        this.targetCodeArea.displaceCaret(this.targetCodeArea.getCaretPosition() - 1);
    }

    private void handleActionJavaKeywords(String keyword) {
        this.doAutoComplete(keyword);
    }
    // ===============
    private void doAutoComplete(String textToInsert) {
        String token = "%";
        String text = targetCodeArea.getText().replace(token, "?");
        int caretPos = targetCodeArea.getCaretPosition();
        int lastIndexOfSymbol01 = text.lastIndexOf(" ",  caretPos - 1);
        int lastIndexOfSymbol02 = text.lastIndexOf("{",  caretPos - 1);
        int lastIndexOfSymbol03 = text.lastIndexOf("(",  caretPos - 1);
        int lastIndexOfSymbol04 = text.lastIndexOf("\n",  caretPos - 1);
        StringBuilder b = new StringBuilder(text);
        if (lastIndexOfSymbol01 != -1) {
            b.replace(lastIndexOfSymbol01, lastIndexOfSymbol01 + 1, token);
        }
        if (lastIndexOfSymbol02 != -1) {
            b.replace(lastIndexOfSymbol02, lastIndexOfSymbol02 + 1, token);
        }
        if (lastIndexOfSymbol03 != -1) {
            b.replace(lastIndexOfSymbol03, lastIndexOfSymbol03 + 1, token);
        }
        if (lastIndexOfSymbol04 != -1) {
            b.replace(lastIndexOfSymbol04, lastIndexOfSymbol04 + 1, token);
        }
        String textReplacedSymbols = b.toString();
        int spacePos = textReplacedSymbols.lastIndexOf(token, caretPos);
        targetCodeArea.replaceText(spacePos + 1, caretPos, textToInsert);
    }

    // ===============
    private void clearAndHideProposalsPopup() {
        proposalsMenu.clear();
        proposalsMenu.hide();
    }

    // ===============
    public void handleKeyPressEvent(KeyEvent keyEvent) {
        clearAndHideProposalsPopup();
        boolean isCtrlSpacePressed = (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.SPACE));
        if (
                (
                        !keyEvent.isShiftDown() && !keyEvent.isAltDown()
                )
                && (
                        (
                                keyEvent.getText().trim().length() == 1 && !keyEvent.isControlDown()
                        )
                        || isCtrlSpacePressed
                )
        ) {
            classMetadataInit();
            StringBuilder word = new StringBuilder();
            int pos = targetCodeArea.getCaretPosition();
            String text = targetCodeArea.getText();
            String currentSymbol;
            if (isCtrlSpacePressed) {
                pos--;
                currentSymbol = text.substring(pos, pos + 1);
            } else {
                currentSymbol = keyEvent.getText();
            }
            while (
                    pos > 0
                            && !currentSymbol.equals(" ")
                            && !currentSymbol.equals("\r")
                            && !currentSymbol.equals("\n")
                            && !currentSymbol.equals("{")
                            && !currentSymbol.equals("(")
                    ) {
                word.insert(0, currentSymbol);
                pos--;
                currentSymbol = text.substring(pos, pos + 1);
            }

            String finalWord = word.toString();

            AutocompleteHelper.javaKeywords.forEach(v -> {
                if (finalWord.trim().isEmpty() || v.toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(v, v);
                    item.setActionHandler(actionHandler -> this.handleActionJavaKeywords(v));
                    proposalsMenu.addItem(item);
                }
            });

            classFields.forEach((k, v) -> {
                String fieldName = ((VariableDeclarationFragment)k.fragments().get(0)).getName().toString();
                if (finalWord.trim().isEmpty() || fieldName.toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(fieldName, v);
                    item.setActionHandler(actionHandler -> this.handleActionFields(fieldName));
                    proposalsMenu.addItem(item);
                }
            });

            parentsFields.forEach((k, v) -> {
                if (finalWord.trim().isEmpty() || k.getName().toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(v, v);
                    item.setActionHandler(actionHandler -> this.handleActionFields(v));
                    proposalsMenu.addItem(item);
                }
            });

            classMethods.forEach((k, v) -> {
                if (finalWord.trim().isEmpty() ||
                        k.getName().getIdentifier().toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(k.getName().getIdentifier(), v);
                    item.setActionHandler(actionHandler -> this.handleActionMethods(k.getName().getIdentifier()));
                    proposalsMenu.addItem(item);
                }
            });

            parentsMethods.forEach((k, v) -> {
                if (finalWord.trim().isEmpty() || k.getName().toUpperCase().contains(finalWord.trim().toUpperCase())) {
                    ProposalItem item = new ProposalItem(k.getName(), v);
                    item.setActionHandler(actionHandler -> this.handleActionMethods(k.getName()));
                    proposalsMenu.addItem(item);
                }
            });

            if (proposalsMenu.isEmpty() && isCtrlSpacePressed) {
                proposalsMenu.addItem(new ProposalItem(null, "No suggestions"));

            }

            if (!proposalsMenu.isEmpty()) {
                proposalsMenu.show(
                        targetCodeArea,
                        targetCodeArea.getCaretBounds().get().getMaxX(),
                        targetCodeArea.getCaretBounds().get().getMaxY()
                );
            }
        }
    }

}
