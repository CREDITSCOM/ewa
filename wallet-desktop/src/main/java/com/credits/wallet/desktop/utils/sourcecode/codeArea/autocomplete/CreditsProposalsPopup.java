package com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete;

import com.credits.wallet.desktop.utils.sourcecode.JavaReflect;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CreditsProposalsPopup extends Popup {
    private ListView<ProposalItem> listView = new ListView();

    private final static Logger LOGGER = LoggerFactory.getLogger(CreditsProposalsPopup.class);

    public static Map<Method, String> parentsMethods = new HashMap<>();
    public static Map<Field, String> parentsFields = new HashMap<>();

    private static void defaultProposalsInit() {
        try {
            getFieldsAndMethodsFromSourceCode("SmartContract");
            getFieldsAndMethodsFromSourceCode("BasicStandard");
            getFieldsAndMethodsFromSourceCode("ExtensionStandard");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error on proposal popup init", e);
        }
    }

    private static void getFieldsAndMethodsFromSourceCode(String parentClass) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(parentClass);
        Map<Field, String> declaredFields = JavaReflect.getDeclaredFields(clazz);
        parentsFields.putAll(declaredFields);
        Map<Method, String> declaredMethods = JavaReflect.getDeclaredMethods(clazz);
        parentsMethods.putAll(declaredMethods);
    }


    public CreditsProposalsPopup() {
        super();
        this.setAutoHide(true);
        defaultProposalsInit();
        listView.setStyle("-fx-border-color: blue; -fx-background-insets: 1");
        listView.setMaxHeight(110);


        this.focusedProperty().addListener((observable, old, newPropertyValue) -> {
            if (!newPropertyValue) {
                this.hide();
            }
        });

        listView.addEventHandler(KeyEvent.KEY_PRESSED, (k) -> {
            if (k.getCode().equals(KeyCode.ENTER)) {
                doProposalItemAction();
            }
            if (k.getCode().equals(KeyCode.TAB)) {
                doFirstItemAction();
            }
            if (k.getCode().equals(KeyCode.ESCAPE)) {
                this.clear();
                this.hide();
            }
        });

        listView.focusedProperty().addListener((observable, old, newPropertyValue) -> {
            if (!newPropertyValue) {
                CreditsProposalsPopup.this.hide();
            }
        });

        listView.addEventHandler(MouseEvent.MOUSE_CLICKED, (k) -> {
            if (k.getButton() == MouseButton.PRIMARY) {
                doProposalItemAction();
            }
        });

        this.getContent().add(listView);
    }

    private void doFirstItemAction() {
        ProposalItem proposalItem = listView.getItems().get(0);
        proposalItem.action();
        this.hide();
    }

    private void doProposalItemAction() {
        ProposalItem proposalItem = listView.getSelectionModel().getSelectedItem();
        proposalItem.action();
        this.hide();
    }

    public void clearAndHide() {
        this.clear();
        this.hide();
    }

    public void addItem(ProposalItem element) {
        listView.getItems().add(element);
    }

    public void clear() {
        listView.getItems().clear();
    }

    public boolean isEmpty() {
        return this.listView.getItems().isEmpty();
    }
}
