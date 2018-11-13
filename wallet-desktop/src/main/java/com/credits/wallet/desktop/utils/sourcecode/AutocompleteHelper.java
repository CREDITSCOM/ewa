package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.wallet.desktop.exception.WalletDesktopException;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.fxmisc.richtext.CodeArea;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutocompleteHelper {

    private static final List<String> javaKeywords = Arrays.asList(
            new String[] {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
                    "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
                    "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                    "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
                    "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"}
                    );

    private CodeArea targetCodeArea;

    private Map<Method, String> parentsMethods = new HashMap<>();

    private Map<MethodDeclaration, String> classMethods = new HashMap<>();

    private Map<Field, String> parentsFields = new HashMap<>();

    private Map<FieldDeclaration, String> classFields = new HashMap<>();

    private ContextMenu contextMenu = new ContextMenu();

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
        ).collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue()
        ));
    }

    private static Map<Field, String> mergeFieldMap(Map<Field, String> firstMap, Map<Field, String> secondMap) {
        return Stream.concat(
                firstMap.entrySet().stream(),
                secondMap.entrySet().stream()
        ).collect(Collectors.toMap(
                entry -> entry.getKey(),
                entry -> entry.getValue()
        ));
    }
    // ===============
    private void updateClassMetadata(String classSource) {

        List<FieldDeclaration> fields = SourceCodeUtils.parseFields(classSource);
        fields.forEach(field -> {
            this.classFields.put(field, field.toString());
        });

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
        String textWONewLines = targetCodeArea.getText().replace("\n", " "); // text without new lines (replaced to space)
        int caretPos = targetCodeArea.getCaretPosition();
        int spacePos = textWONewLines.lastIndexOf(" ", caretPos - 2);
        targetCodeArea.replaceText(spacePos + 1, caretPos, textToInsert);
    }

    // ===============
    private void clearAndHideProposalsPopup() {
        contextMenu.getItems().clear();
        contextMenu.hide();
    }

    private void showProposalsPopup(String enteredChar) {

        String word = "";
        int pos = targetCodeArea.getCaretPosition();
        String text = targetCodeArea.getText();
        String currentSymbol;
        if (enteredChar.trim().length() == 0) {
            pos--;
            currentSymbol = text.substring(pos, pos + 1);
        } else {
            currentSymbol = enteredChar;
        }
        while (
                pos > 0
                        && !currentSymbol.equals(" ")
                        && !currentSymbol.equals("\r")
                        && !currentSymbol.equals("\n")
                ) {
            word = currentSymbol + word;
            pos--;
            currentSymbol = text.substring(pos, pos + 1);
        }

        String finalWord = word;

        AutocompleteHelper.javaKeywords.forEach(v -> {
            if (finalWord.trim().isEmpty() || v.toUpperCase().indexOf(finalWord.trim().toUpperCase()) != -1) {
                MenuItem action = new MenuItem(v);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> this.handleActionJavaKeywords(v));
            }
        });

        classFields.forEach((k, v) -> {
            String fieldName = ((VariableDeclarationFragment)k.fragments().get(0)).getName().toString();
            if (finalWord.trim().isEmpty() || fieldName.toUpperCase().indexOf(finalWord.trim().toUpperCase()) != -1) {
                MenuItem action = new MenuItem(v);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> this.handleActionFields(fieldName));
            }
        });

        parentsFields.forEach((k, v) -> {
            if (finalWord.trim().isEmpty() || k.getName().toUpperCase().indexOf(finalWord.trim().toUpperCase()) != -1) {
                MenuItem action = new MenuItem(v);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> handleActionFields(k.getName()));
            }
        });

        classMethods.forEach((k, v) -> {
            if (finalWord.trim().isEmpty() || k.getName().getIdentifier().toUpperCase().indexOf(finalWord.trim().toUpperCase()) != -1) {
                MenuItem action = new MenuItem(v);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> this.handleActionMethods(k.getName().getIdentifier()));
            }
        });

        parentsMethods.forEach((k, v) -> {
            if (finalWord.trim().isEmpty() || k.getName().toUpperCase().indexOf(finalWord.trim().toUpperCase()) != -1) {
                MenuItem action = new MenuItem(v);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> handleActionMethods(k.getName()));
            }
        });

        if (contextMenu.getItems().isEmpty()) {
            MenuItem action = new MenuItem("No suggestions");
            contextMenu.getItems().add(action);
        }

        contextMenu.show(targetCodeArea, targetCodeArea.getCaretBounds().get().getMaxX(),
                targetCodeArea.getCaretBounds().get().getMaxY());
    }
    // ===============
    public void handleKeyPressEvent(KeyEvent keyEvent) {
        clearAndHideProposalsPopup();
        if (
                (
                        !keyEvent.isShiftDown() && !keyEvent.isAltDown()
                )
                        && (
                                (
                                        keyEvent.getText().trim().length() == 1 && !keyEvent.isControlDown()
                                )
                                || (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.SPACE))
                )
                ) {
            classMetadataInit();
            showProposalsPopup(keyEvent.getText());
        }
    }

}
