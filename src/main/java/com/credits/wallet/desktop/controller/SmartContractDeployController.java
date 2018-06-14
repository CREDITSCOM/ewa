package com.credits.wallet.desktop.controller;

import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.utils.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
//TODO: This class is a GODZILLA please refactor it ASAP!
public class SmartContractDeployController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);

    private static final String DEFAULT_SOURCE_CODE =
        "public class Contract extends SmartContract {\n" + "\n" + "    public Contract() {\n" +
            "        total = 0;\n" + "    }" + "\n" + "}";
    private static final String NON_CHANGED_STR = "public class Contract extends SmartContract {";

    private static final String[] PARENT_METHODS =
        new String[] {"double total", "Double getBalance(String address, String currency)",
            "TransactionData getTransaction(String transactionId)",
            "List<TransactionData> getTransactions(String address, long offset, long limit)",
            "List<PoolData> getPoolList(long offset, long limit)", "PoolData getPool(String poolNumber)",
            "void sendTransaction(String source, String target, Double amount, String currency)"};

    private CodeArea codeArea;
    private TableView tabErrors;

    private String prevCode;

    @FXML
    private Pane paneCode;

    @FXML
    private TreeView<Label> classTreeView;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        this.prevCode = DEFAULT_SOURCE_CODE;

        this.codeArea = new CodeArea();
        this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.codeArea));

        this.codeArea.setOnKeyPressed(ke -> {
            if (ke.isControlDown() && ke.getCode().equals(KeyCode.SPACE)) {
                codeCompletionPopup();
            }
        });

        this.codeArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                    String curCode = this.codeArea.getText();

                    // Replace TAB to 4 spaces
                    if (curCode.contains("\t")) {
                        this.codeArea.replaceText(0, curCode.length(), curCode.replace("\t", "    "));
                        curCode = this.codeArea.getText();
                    }

                    if (!curCode.contains(NON_CHANGED_STR)) {
                        this.codeArea.replaceText(0, curCode.length(), this.prevCode);
                    } else {
                        int i1 = curCode.indexOf(NON_CHANGED_STR);
                        if (curCode.indexOf(NON_CHANGED_STR, i1 + 1) > 0) {
                            this.codeArea.replaceText(0, curCode.length(), this.prevCode);
                        }
                    }
                    this.prevCode = this.codeArea.getText();
                });

        this.codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(this.codeArea.richChanges())
                .filterMap(t -> {
                    if (t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);

        this.codeArea.setPrefHeight(this.paneCode.getPrefHeight());
        this.codeArea.setPrefWidth(this.paneCode.getPrefWidth());
        this.codeArea.replaceText(0, 0, DEFAULT_SOURCE_CODE);
        this.paneCode.getChildren().add(this.codeArea);

        this.tabErrors = new TableView();
        this.tabErrors.setPrefHeight(this.paneCode.getPrefHeight() * 0.3);
        this.tabErrors.setPrefWidth(this.paneCode.getPrefWidth());

        TableColumn tabErrorsColLine = new TableColumn();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setPrefWidth(this.paneCode.getPrefWidth() * 0.1);
        TableColumn tabErrorsColText = new TableColumn();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setPrefWidth(this.paneCode.getPrefWidth() * 0.9);
        this.tabErrors.getColumns().add(tabErrorsColLine);
        this.tabErrors.getColumns().add(tabErrorsColText);

        TableColumn[] tableColumns = new TableColumn[this.tabErrors.getColumns().size()];
        for (int i = 0; i < this.tabErrors.getColumns().size(); i++) {
            tableColumns[i] = (TableColumn) this.tabErrors.getColumns().get(i);
        }
        tableColumns[0].setCellValueFactory(new PropertyValueFactory<ErrorCodeTabRow, String>("line"));
        tableColumns[1].setCellValueFactory(new PropertyValueFactory<ErrorCodeTabRow, String>("text"));

        this.tabErrors.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                ErrorCodeTabRow tabRow = (ErrorCodeTabRow) this.tabErrors.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    positionCursorToLine(Integer.valueOf(tabRow.getLine()));
                }
            }
        });

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
        StringBuilder sb = new StringBuilder();
        sb.append("CST");
        sb.append(com.credits.common.utils.Utils.randomAlphaNumeric(29));
        String token = sb.toString();

        // Parse className
        String className = SourceCodeUtils.parseClassName(this.codeArea.getText(), "SmartContract");
        try {
            String javaCode = SourceCodeUtils.normalizeSourceCode(this.codeArea.getText());
            byte[] byteCode = SimpleInMemoryCompiler.compile(javaCode, className, token);
            String hashState = ApiUtils.generateSmartContractHashState(byteCode);
            SmartContractData smartContractData = new SmartContractData(token, javaCode, byteCode, hashState);
            String transactionInnerId = ApiUtils.generateTransactionInnerId();
            ApiResponseData apiResponseData =
                AppState.apiClient.deploySmartContract(transactionInnerId, AppState.account, "", smartContractData);

            if (apiResponseData.getCode() == ApiClient.API_RESPONSE_SUCCESS_CODE) {
                StringSelection selection = new StringSelection(token);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                Utils.showInfo(String.format("Token\n\n%s\n\nhas generated and copied to clipboard", token));
            } else {
                Utils.showError(String.format("Error deploying smart contract: %s", apiResponseData.getMessage()));
            }
        } catch (Exception e) {
            LOGGER.error("Error deploying smart contract " + e.toString(), e);
            Utils.showError("Error deploying smart contract " + e.toString());
        }
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {

        this.classTreeView.setRoot(null);

        String sourceCode = this.codeArea.getText();

        CompilationUnit compilationUnit = EclipseJdt.createCompilationUnit(sourceCode);

        List typeList = compilationUnit.types();

        if (typeList.size() != 1) {
            return;
        }

        TypeDeclaration typeDeclaration = (TypeDeclaration) typeList.get(0);

        String className = (typeDeclaration).getName().getFullyQualifiedName();

        Label labelRoot = new Label(className);

        TreeItem<Label> treeRoot = new TreeItem<>(labelRoot);

        ASTNode root = compilationUnit.getRoot();

        root.accept(new ASTVisitor() {

            @Override
            public boolean visit(FieldDeclaration node) {
                return true;
            }

            @Override
            public void endVisit(FieldDeclaration node) {
                Label label = new Label(node.toString());

                label.setOnMousePressed(event -> {
                    if (event.isPrimaryButtonDown()) {
                        positionCursorToLine(compilationUnit.getLineNumber(node.getStartPosition()));
                    }
                });

                TreeItem<Label> treeItem = new TreeItem<>();
                treeItem.setValue(label);

                treeRoot.getChildren().add(treeItem);
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                return true;
            }

            @Override
            public void endVisit(MethodDeclaration node) {
                node.setBody(null);
                Label label = new Label(node.toString());
                label.setOnMousePressed(event -> {
                    if (event.isPrimaryButtonDown()) {
                        positionCursorToLine(compilationUnit.getLineNumber(node.getStartPosition()));
                    }
                });

                TreeItem<Label> treeItem = new TreeItem<>();
                treeItem.setValue(label);

                treeRoot.getChildren().add(treeItem);
            }
        });

        treeRoot.setExpanded(true);
        this.classTreeView.setRoot(treeRoot);
    }

    @FXML
    @SuppressWarnings("unchecked")
    private void checkButtonAction() {
        String sourceCode = this.codeArea.getText();

        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);

        if (problemArr.length > 0) {
            this.tabErrors.getItems().clear();

            for (IProblem p : problemArr) {
                ErrorCodeTabRow tr = new ErrorCodeTabRow();
                tr.setLine(Integer.toString(p.getSourceLineNumber()));
                tr.setText(p.getMessage());
                this.tabErrors.getItems().add(tr);
            }

            this.codeArea.setPrefHeight(this.paneCode.getPrefHeight() * 0.7);
            this.paneCode.getChildren().clear();
            this.paneCode.getChildren().add(this.codeArea);
            this.paneCode.getChildren().add(this.tabErrors);
            this.paneCode.getChildren().get(1).setLayoutY(this.paneCode.getPrefHeight() * 0.7);
        } else {
            this.codeArea.setPrefHeight(this.paneCode.getPrefHeight());
            this.paneCode.getChildren().clear();
            this.paneCode.getChildren().add(this.codeArea);
        }
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        this.codeArea.setStyleSpans(0, highlighting);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String sourceCode = this.codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return SourceCodeUtils.computeHighlighting(sourceCode);
            }
        };
        AppState.executor.execute(task);
        return task;
    }

    private void positionCursorToLine(int line) {
        char[] text = this.codeArea.getText().toCharArray();
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
        this.codeArea.displaceCaret(pos);
        this.codeArea.showParagraphAtTop(Math.max(0, line - 5));
        this.codeArea.requestFocus();
    }

    private void codeCompletionPopup() {
        ContextMenu contextMenu = new ContextMenu();

        String word = "";
        int pos = this.codeArea.getCaretPosition() - 1;
        String txt = this.codeArea.getText();
        while (pos > 0 && !txt.substring(pos, pos + 1).equals(" ") && !txt.substring(pos, pos + 1).equals("\r") &&
            !txt.substring(pos, pos + 1).equals("\n")) {
            word = txt.substring(pos, pos + 1) + word;
            pos--;
        }

        for (String method : PARENT_METHODS) {
            if (word.trim().isEmpty() || method.toUpperCase().indexOf(word.trim().toUpperCase()) > 0) {
                MenuItem action = new MenuItem(method);
                contextMenu.getItems().add(action);
                action.setOnAction(event -> {
                    int pos1 = this.codeArea.getCaretPosition();
                    String txtToIns = SourceCodeUtils.normalizeMethodName(method);
                    this.codeArea.replaceText(pos1, pos1, txtToIns);
                });
            }
        }

        if (contextMenu.getItems().isEmpty()) {
            MenuItem action = new MenuItem("No suggestions");
            contextMenu.getItems().add(action);
        }

        contextMenu.show(this.codeArea, this.codeArea.getCaretBounds().get().getMaxX(),
            this.codeArea.getCaretBounds().get().getMaxY());
    }
}