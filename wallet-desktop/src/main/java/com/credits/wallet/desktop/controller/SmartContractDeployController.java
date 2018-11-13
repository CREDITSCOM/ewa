package com.credits.wallet.desktop.controller;

import com.credits.general.exception.CompilationException;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.util.Callback;
import com.credits.general.util.Converter;
import com.credits.general.util.compiler.InMemoryCompiler;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.exception.WalletDesktopException;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import com.credits.wallet.desktop.utils.TransactionIdCalculateUtils;
import com.credits.wallet.desktop.utils.sourcecode.AutocompleteHelper;
import com.credits.wallet.desktop.utils.sourcecode.EclipseJdt;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.general.util.Converter.decodeFromBASE58;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
//TODO: This class is a GODZILLA please refactor it ASAP!
public class SmartContractDeployController implements Initializable {

    public static final String BUILD = "Build";
    public static final String COMPILING = "Compiling...";
    //    private static final String NON_CHANGED_STR = "public class Contract extends SmartContract {";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);
    private static final String CLASS_NAME = "Contract";
    private static final String SUPERCLASS_NAME = "SmartContract";

    private CodeArea codeArea;


    @FXML
    private TableView<ErrorCodeTabRow> errorTableView;

    @FXML
    private SplitPane splitPane;

    @FXML
    BorderPane bp;

    @FXML
    private Pane paneCode;

    @FXML
    private Pane debugPane;

    @FXML
    private TreeView<Label> classTreeView;

    @FXML
    public Button deployButton;

    @FXML
    public Button checkButton;

    private AutocompleteHelper autocompleteHelper;

    public CompilationPackage compilationPackage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        FormUtils.resizeForm(bp);

        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        codeArea = SmartContractUtils.initCodeArea(paneCode, false);

        SmartContractUtils.initCodeAreaLogic(codeArea);

        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (evt) -> {
            deployButton.setDisable(true);
            checkButton.setDisable(false);
        });

        try {
            autocompleteHelper = AutocompleteHelper.init(codeArea);
        } catch (WalletDesktopException e) {
            LOGGER.error("", e);
            FormUtils.showError(e.getMessage());
        }

        for (SplitPane.Divider d : splitPane.getDividers()) {
            d.positionProperty()
                .addListener((observable, oldValue, newValue) -> errorTableView.setPrefHeight(debugPane.getHeight()));
        }

        panelCodeKeyReleased();
        initErrorTableView();

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
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT);
    }

    @FXML
    private void panelCodeKeyReleased() {
        Thread t = new Thread(this::refreshClassMembersTree);
        t.setDaemon(true);
        t.start();
    }

    private synchronized void refreshClassMembersTree() {
        Platform.runLater(() -> {
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
        });
    }

    @FXML
    private void handleCheck() {
        checkButton.setText(COMPILING);
        checkButton.setDisable(true);
        errorTableView.setVisible(false);
        CompletableFuture.supplyAsync(() -> buildSourceCode(codeArea.getText()))
            .whenComplete(handleCallback(handleCheckResult()));
    }


    private Callback<CompilationResult> handleCheckResult() {
        return new Callback<CompilationResult>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(CompilationResult compilationResult) {
                List listOfError = compilationResult.getErrors();
                Platform.runLater(() -> {
                    checkButton.setText(BUILD);
                });

                if (listOfError.size() > 0) {
                    Platform.runLater(() -> {
                        checkButton.setDisable(false);
                        errorTableView.getItems().clear();
                        errorTableView.getItems().addAll(listOfError);
                        errorTableView.setVisible(true);
                    });
                } else {
                    compilationPackage = compilationResult.getCompilationPackage();
                    Platform.runLater(() -> {
                        checkButton.setDisable(true);
                        deployButton.setDisable(false);
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(() -> {
                    checkButton.setDisable(false);
                    checkButton.setText(BUILD);
                    FormUtils.showPlatformError(e.getMessage());
                });
                LOGGER.error("failed!", e);
            }
        };
    }

    private CompilationResult buildSourceCode(String sourceCode) {
        CompilationPackage compilationPackage = null;
        String className = SourceCodeUtils.parseClassName(sourceCode, "");
        List<ErrorCodeTabRow> listOfError = new ArrayList<>();
        try {
            this.checkClassAndSuperclassNames(className, sourceCode);
        } catch (CreditsException e) {
            ErrorCodeTabRow tr = new ErrorCodeTabRow();
            tr.setLine("1");
            tr.setText(e.getMessage());
            listOfError.add(tr);
        }
        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);
        if (problemArr.length > 0) {

            for (IProblem p : problemArr) {
                ErrorCodeTabRow tr = new ErrorCodeTabRow();
                tr.setLine(Integer.toString(p.getSourceLineNumber()));
                tr.setText(p.getMessage());
                listOfError.add(tr);
            }
        } else {
             compilationPackage = new InMemoryCompiler().compile(className, sourceCode);
            if (!compilationPackage.isCompilationStatusSuccess()) {
                DiagnosticCollector collector = compilationPackage.getCollector();
                List<Diagnostic> diagnostics = collector.getDiagnostics();
                diagnostics.forEach(action -> {
                    ErrorCodeTabRow tr = new ErrorCodeTabRow();
                    tr.setLine(Converter.toString(action.getLineNumber()));
                    tr.setText(action.getMessage(null));
                    listOfError.add(tr);
                });
            }
        }
        return new CompilationResult(compilationPackage,listOfError);
    }

    private class CompilationResult {
        List<ErrorCodeTabRow> errorCodeTabRows;
        CompilationPackage compilationPackage;

        public CompilationResult(CompilationPackage compilationPackage, List<ErrorCodeTabRow> listOfError) {
            this.errorCodeTabRows = listOfError;
            this.compilationPackage = compilationPackage;
        }

        public List<ErrorCodeTabRow> getErrors() {
            return errorCodeTabRows;
        }

        public void setErrorCodeTabRows(List<ErrorCodeTabRow> errorCodeTabRows) {
            this.errorCodeTabRows = errorCodeTabRows;
        }

        public CompilationPackage getCompilationPackage() {
            return compilationPackage;
        }

        public void setCompilationPackage(CompilationPackage compilationPackage) {
            this.compilationPackage = compilationPackage;
        }
    }

    private void addTabErrorsToDebugPane() {
        errorTableView.setPrefHeight(30 + errorTableView.getItems().size() * 25);
        errorTableView.setVisible(true);
    }

    @FXML
    private void handleDeploy() {

        String className = SourceCodeUtils.parseClassName(codeArea.getText(), "SmartContract");
        try {
            String javaCode = SourceCodeUtils.normalizeSourceCode(codeArea.getText());
            CompilationPackage compilationPackage = new InMemoryCompiler().compile(className, javaCode);

            if (compilationPackage.isCompilationStatusSuccess()) {
                List<CompilationUnit> compilationUnits = compilationPackage.getUnits();
                CompilationUnit compilationUnit = compilationUnits.get(0);
                byte[] byteCode = compilationUnit.getBytecode();

                SmartContractData smartContractData =
                    new SmartContractData(SmartContractUtils.generateSmartContractAddress(), decodeFromBASE58(account),
                        javaCode, byteCode, null);

                CompletableFuture.supplyAsync(() -> TransactionIdCalculateUtils.calcTransactionIdSourceTarget(account,
                    smartContractData.getBase58Address()), threadPool)
                    .thenApply((transactionData) -> createSmartContractTransaction(transactionData, smartContractData))
                    .whenComplete(handleCallback(handleDeployResult()));
                AppState.lastSmartContract = codeArea.getText();
                VistaNavigator.loadVista(VistaNavigator.WALLET);
            } else {
                DiagnosticCollector collector = compilationPackage.getCollector();
                List<Diagnostic> diagnostics = collector.getDiagnostics();
                StringBuilder errors = new StringBuilder();
                diagnostics.forEach(action -> errors.append(
                    String.format("line %s, error %s; ", action.getLineNumber(), action.getMessage(null))));
                throw new CompilationException(String.format("Compilation errors: %s", errors.toString()));
            }
        } catch (CreditsException e) {
            LOGGER.error("failed!", e);
            FormUtils.showError(AppState.NODE_ERROR + ": " + e.getMessage());
        }
    }

    private Callback<ApiResponseData> handleDeployResult() {
        return new Callback<ApiResponseData>() {
            @Override
            public void onSuccess(ApiResponseData resultData) {
                ApiUtils.saveTransactionRoundNumberIntoMap(resultData);
                String target = resultData.getTarget();
                StringSelection selection = new StringSelection(target);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                FormUtils.showPlatformInfo(
                    String.format("Smart-contract address\n\n%s\n\nhas generated and copied to clipboard", target));
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showPlatformError(e.getMessage());
            }
        };
    }

    private void checkClassAndSuperclassNames(String className, String sourceCode) throws CreditsException {
        if (!className.equals(CLASS_NAME)) {
            throw new CreditsException(
                String.format("Wrong class name %s, class name must be %s", className, CLASS_NAME));
        }
        String superclassName = SourceCodeUtils.parseSuperclassName(sourceCode);

        if (superclassName == null || !superclassName.equals(SUPERCLASS_NAME)) {
            throw new CreditsException(
                String.format("Wrong superclass name %s, superclass name must be %s", superclassName, SUPERCLASS_NAME));
        }
    }

    private void initErrorTableView() {
        TableColumn<ErrorCodeTabRow, String> tabErrorsColLine = new TableColumn<>();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setCellValueFactory(new PropertyValueFactory<>("line"));
        tabErrorsColLine.setPrefWidth(debugPane.getPrefWidth() * 0.1);

        TableColumn<ErrorCodeTabRow, String> tabErrorsColText = new TableColumn<>();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setCellValueFactory(new PropertyValueFactory<>("text"));
        tabErrorsColText.setPrefWidth(debugPane.getPrefWidth() * 0.88);

        errorTableView.setVisible(false);
        errorTableView.setPrefHeight(debugPane.getPrefHeight());
        errorTableView.setPrefWidth(debugPane.getPrefWidth());

        errorTableView.getColumns().add(tabErrorsColLine);
        errorTableView.getColumns().add(tabErrorsColText);

        errorTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                ErrorCodeTabRow tabRow = errorTableView.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    positionCursorToLine(Integer.valueOf(tabRow.getLine()));
                }
            }
        });
    }

}