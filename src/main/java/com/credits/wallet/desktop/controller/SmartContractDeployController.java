package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.sourcecode.SourceCodeUtils;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.util.ApiClientUtils;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.exception.CompilationException;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.utils.*;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
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

        this.codeArea = SmartContractUtils.initCodeArea(this.paneCode);

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

        this.codeArea.replaceText(0, 0, DEFAULT_SOURCE_CODE);

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
        String token = SourceCodeUtils.generateSmartContractToken();
        String className = SourceCodeUtils.parseClassName(this.codeArea.getText(), "SmartContract");
        try {
            String javaCode = SourceCodeUtils.normalizeSourceCode(this.codeArea.getText());
            byte[] byteCode = SimpleInMemoryCompiler.compile(javaCode, className, token);
            String hashState = ApiUtils.generateSmartContractHashState(byteCode);
            SmartContractData smartContractData = new SmartContractData(token, javaCode, byteCode, null, hashState, null, null);
            long transactionInnerId = ApiUtils.generateTransactionInnerId();
            String transactionTarget = generatePublicKeyBase58();
            LOGGER.info("transactionTarget = {}", transactionTarget);

            ByteBuffer signature;
            try {
                TransactionStruct tStruct = new TransactionStruct(transactionInnerId, AppState.account, transactionTarget,
                        new BigDecimal(0), new BigDecimal(0), (byte)1, smartContractData.getByteCode());
                byte[] tArr=tStruct.getBytes();

                LOGGER.debug("Transaction structure ^^^^^ ");
                String arrStr="";
                for (int i=0; i<tArr.length; i++)
                    arrStr=arrStr+((i==0 ? "" : ", ")+tArr[i]);
                LOGGER.debug(arrStr);
                LOGGER.debug("--------------------- vvvvv ");

                byte[] signatureArr=Ed25519.sign(tArr, AppState.privateKey);

                LOGGER.debug("Signature ^^^^^ ");
                arrStr="";
                for (int i=0; i<signatureArr.length; i++)
                    arrStr=arrStr+((i==0 ? "" : ", ")+signatureArr[i]);
                LOGGER.debug(arrStr);
                LOGGER.debug("--------- vvvvv ");

                signature = ByteBuffer.wrap(signatureArr);

            } catch (Exception e) {
                signature = ByteBuffer.wrap(new byte[]{});
            }

            ApiResponseData apiResponseData = AppState.apiClient.deploySmartContract(transactionInnerId, AppState.account, transactionTarget, smartContractData, signature);
            if (apiResponseData.getCode() == ApiClient.API_RESPONSE_SUCCESS_CODE) {
                StringSelection selection = new StringSelection(token);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                Utils.showInfo(String.format("Token\n\n%s\n\nhas generated and copied to clipboard", token));
            } else {
                Utils.showError(String.format("Error deploying smart contract: %s", apiResponseData.getMessage()));
            }
        } catch (LevelDbClientException e) {
            LOGGER.error(e.toString(), e);
            Utils.showError(AppState.NODE_ERROR + ": "+e.getMessage());
        } catch (CreditsNodeException e) {
            LOGGER.error(e.toString(), e);
            Utils.showError(AppState.NODE_ERROR + ": "+e.getMessage());
        } catch (CreditsException e) {
            LOGGER.error(e.toString(), e);
            Utils.showError(AppState.NODE_ERROR + ": "+e.getMessage());
        } catch (CompilationException e) {
            LOGGER.error(e.toString(), e);
            Utils.showError(e.getMessage());
        }
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {

        this.classTreeView.setRoot(null);
        String sourceCode = this.codeArea.getText();
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
            if (classMember instanceof  MethodDeclaration) {
                ((MethodDeclaration)classMember).setBody(null);
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

    private String generatePublicKeyBase58() {
        KeyPair keyPair = Ed25519.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        return Converter.encodeToBASE58(Ed25519.publicKeyToBytes(publicKey));
    }
}