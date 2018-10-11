package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.CompilationException;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.EclipseJdt;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SimpleInMemoryCompiler;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
//TODO: This class is a GODZILLA please refactor it ASAP!
public class SmartContractDeployController extends Controller implements Initializable {

    private static final String DEFAULT_SOURCE_CODE =
        "public class Contract extends SmartContract {\n" + "\n" + "    public Contract() {\n" + "        total = 0;\n" + "    }" + "\n" + "}";
    private static final String[] PARENT_METHODS =
        new String[] {"double total", "Double getBalance(String address, String currency)", "TransactionData getTransaction(String transactionId)",
            "List<TransactionData> getTransactions(String address, long offset, long limit)", "List<PoolData> getPoolList(long offset, long limit)",
            "PoolData getPool(String poolNumber)", "void sendTransaction(String account, String target, Double amount, String currency)"};
    //    private static final String NON_CHANGED_STR = "public class Contract extends SmartContract {";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);
    private CodeArea codeArea;
    private TableView<ErrorCodeTabRow> tabErrors;
    private int tabCount;

    @FXML
    private Pane paneCode;

    @FXML
    private ScrollPane scCodePanel;

    @FXML
    private TreeView<Label> classTreeView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        codeArea = SmartContractUtils.initCodeArea(paneCode);

        codeArea.setOnKeyPressed(k -> {
            KeyCode code = k.getCode();
            if (code != KeyCode.TAB){
                if(code.isLetterKey() || code.isDigitKey() || code.isNavigationKey() || code.isWhitespaceKey()){
                    tabCount = 0;
                }
            }

            if (k.isControlDown() && k.getCode().equals(KeyCode.SPACE)) {
                codeCompletionPopup();
            }
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

        codeArea.replaceText(0, 0, DEFAULT_SOURCE_CODE);

        tabErrors = new TableView<>();
        tabErrors.setPrefHeight(scCodePanel.getPrefHeight() * 0.3);
        tabErrors.setPrefWidth(scCodePanel.getPrefWidth());

        TableColumn<ErrorCodeTabRow, String> tabErrorsColLine = new TableColumn<>();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setCellValueFactory(new PropertyValueFactory<>("line"));
        tabErrorsColLine.setPrefWidth(scCodePanel.getPrefWidth() * 0.1);

        TableColumn<ErrorCodeTabRow, String> tabErrorsColText = new TableColumn<>();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setCellValueFactory(new PropertyValueFactory<>("text"));
        tabErrorsColText.setPrefWidth(scCodePanel.getPrefWidth() * 0.9);

        tabErrors.getColumns().add(tabErrorsColLine);
        tabErrors.getColumns().add(tabErrorsColText);

        tabErrors.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                ErrorCodeTabRow tabRow = tabErrors.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    positionCursorToLine(Integer.valueOf(tabRow.getLine()));
                }
            }
        });

    }

    private void codeCompletionPopup() {
        ContextMenu contextMenu = new ContextMenu();

        String word = "";
        int pos = codeArea.getCaretPosition() - 1;
        String txt = codeArea.getText();
        while (pos > 0 && !txt.substring(pos, pos + 1).equals(" ") && !txt.substring(pos, pos + 1).equals("\r") && !txt.substring(pos, pos + 1).equals("\n")) {
            word = txt.substring(pos, pos + 1) + word;
            pos--;
        }

        for (String method : PARENT_METHODS) {
            if (word.trim().isEmpty() || method.toUpperCase().indexOf(word.trim().toUpperCase()) > 0) {
                MenuItem action = new MenuItem(method);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> {
                    int pos1 = codeArea.getCaretPosition();
                    String txtToIns = SourceCodeUtils.normalizeMethodName(method);
                    codeArea.replaceText(pos1, pos1, txtToIns);
                });
            }
        }

        if (contextMenu.getItems().isEmpty()) {
            MenuItem action = new MenuItem("No suggestions");
            contextMenu.getItems().add(action);
        }

        contextMenu.show(codeArea, codeArea.getCaretBounds().get().getMaxX(), codeArea.getCaretBounds().get().getMaxY());
    }

    private void positionCursorToLine(int line) {
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

    @FXML
    private void handleBack() {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
            AppState.executor = null;
        }
        App.showForm("/fxml/smart_contract.fxml", "Wallet");
    }

    @FXML
    private void handleDeploy() {
        String token = SourceCodeUtils.generateSmartContractToken();
        String className = SourceCodeUtils.parseClassName(codeArea.getText(), "SmartContract");
        try {
            String javaCode = SourceCodeUtils.normalizeSourceCode(codeArea.getText());
            byte[] byteCode = SimpleInMemoryCompiler.compile(javaCode, className, token);
            String hashState = ApiUtils.generateSmartContractHashState(byteCode);

            ApiUtils.deploySmartContractProcess(javaCode, byteCode, hashState);
        } catch (CompilationException | CreditsException e) {
            LOGGER.error(e.toString(), e);
            FormUtils.showError(AppState.NODE_ERROR + ": " + e.getMessage());
        }
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {

        classTreeView.setRoot(null);
        String sourceCode = codeArea.getText();
        String className = SourceCodeUtils.parseClassName(sourceCode);
        Label labelRoot = new Label(className);
        TreeItem<Label> treeRoot = new TreeItem<>(labelRoot);

        List<FieldDeclaration> fields = SourceCodeUtils.parseFields(sourceCode);
        List<MethodDeclaration> constructors = SourceCodeUtils.parseConstructors(sourceCode);
        List<MethodDeclaration> methods = SourceCodeUtils.parseMethods(sourceCode);

        List<BodyDeclaration> classMembers = new ArrayList<>();
        classMembers.addAll(fields);
        classMembers.addAll(constructors);
        classMembers.addAll(methods);

        classMembers.forEach(classMember -> {
            if (classMember instanceof MethodDeclaration) {
                ((MethodDeclaration) classMember).setBody(null);
            }

            Label label = new Label(classMember.toString());
            label.setOnMousePressed(event -> {
                if (event.isPrimaryButtonDown()) {
                    positionCursorToLine(SourceCodeUtils.getLineNumber(sourceCode, classMember));
                }
            });
            TreeItem<Label> treeItem = new TreeItem<>();
            treeItem.setValue(label);
            treeRoot.getChildren().add(treeItem);
        });

        treeRoot.setExpanded(true);
        classTreeView.setRoot(treeRoot);
    }

    @FXML
    @SuppressWarnings("unchecked")
    private void checkButtonAction() {
        String sourceCode = codeArea.getText();

        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);

        if (problemArr.length > 0) {
            tabErrors.getItems().clear();

            for (IProblem p : problemArr) {
                ErrorCodeTabRow tr = new ErrorCodeTabRow();
                tr.setLine(Integer.toString(p.getSourceLineNumber()));
                tr.setText(p.getMessage());
                tabErrors.getItems().add(tr);
            }

            codeArea.setPrefHeight(paneCode.getPrefHeight() * 0.7);
            paneCode.getChildren().clear();
            paneCode.getChildren().add(codeArea);
            paneCode.getChildren().add(tabErrors);
            paneCode.getChildren().get(1).setLayoutY(paneCode.getPrefHeight() * 0.7);
        } else {
            codeArea.setPrefHeight(paneCode.getPrefHeight());
            paneCode.getChildren().clear();
            paneCode.getChildren().add(codeArea);
            FormUtils.showInfo("Everything is OK");
        }
    }
}