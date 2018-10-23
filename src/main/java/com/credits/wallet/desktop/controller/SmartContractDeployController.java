package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.exception.CompilationException;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.EclipseJdt;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import com.credits.wallet.desktop.utils.compiler.InMemoryCompiler;
import com.credits.wallet.desktop.utils.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.utils.compiler.model.CompilationUnit;
import javafx.application.Platform;
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
import javafx.scene.layout.BorderPane;
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

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
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
    private static final String CLASS_NAME = "Contract";
    private static final String SUPERCLASS_NAME = "SmartContract";

    private CodeArea codeArea;
    private TableView<ErrorCodeTabRow> tabErrors;
    private int tabCount;
    @FXML
    BorderPane bp;

    @FXML
    private Pane paneCode;

    @FXML
    private Pane debugPane;

    @FXML
    private TreeView<Label> classTreeView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        FormUtils.resizeForm(bp);

        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        codeArea = SmartContractUtils.initCodeArea(paneCode, false);

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

        this.tabErrors = new TableView<>();
        tabErrors.getStyleClass().add("credits-history");
        this.tabErrors.setPrefHeight(debugPane.getPrefHeight());
        this.tabErrors.setPrefWidth(debugPane.getPrefWidth());

        TableColumn<ErrorCodeTabRow, String> tabErrorsColLine = new TableColumn<>();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setCellValueFactory(new PropertyValueFactory<>("line"));
        tabErrorsColLine.setPrefWidth(debugPane.getPrefWidth() * 0.1);

        TableColumn<ErrorCodeTabRow, String> tabErrorsColText = new TableColumn<>();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setCellValueFactory(new PropertyValueFactory<>("text"));
        tabErrorsColText.setPrefWidth(debugPane.getPrefWidth() * 0.88);

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
    @SuppressWarnings("unchecked")
    private void handleCheck() {
        tabErrors.getItems().clear();
        debugPane.getChildren().clear();

        String sourceCode = codeArea.getText();

        String className = SourceCodeUtils.parseClassName(sourceCode, "");

        try {
            this.checkClassAndSuperclassNames(className, sourceCode);
        } catch (CreditsException e) {
            ErrorCodeTabRow tr = new ErrorCodeTabRow();
            tr.setLine("1");
            tr.setText(e.getMessage());
            tabErrors.getItems().add(tr);
            addTabErrorsToDebugPane();
            return;
        }
        IProblem[] problemArr = EclipseJdt.checkSyntax(sourceCode);
        if (problemArr.length > 0) {

            for (IProblem p : problemArr) {
                ErrorCodeTabRow tr = new ErrorCodeTabRow();
                tr.setLine(Integer.toString(p.getSourceLineNumber()));
                tr.setText(p.getMessage());
                tabErrors.getItems().add(tr);
            }
            addTabErrorsToDebugPane();
        } else {

            CompilationPackage compilationPackage = new InMemoryCompiler().compile(className, sourceCode);
            if (!compilationPackage.isCompilationStatusSuccess()) {
                DiagnosticCollector collector = compilationPackage.getCollector();
                List<Diagnostic> diagnostics = collector.getDiagnostics();
                diagnostics.forEach(action -> {
                    ErrorCodeTabRow tr = new ErrorCodeTabRow();
                    tr.setLine(Converter.toString(action.getLineNumber()));
                    tr.setText(action.getMessage(null));
                    tabErrors.getItems().add(tr);
                });
                addTabErrorsToDebugPane();
            } else {
                debugPane.getChildren().clear();
                FormUtils.showInfo("Everything is OK");
            }
        }
    }

    private void addTabErrorsToDebugPane() {
        this.tabErrors.setPrefHeight(30+this.tabErrors.getItems().size()*25);
        debugPane.getChildren().clear();
        debugPane.getChildren().add(this.tabErrors);
    }

    @FXML
    private void handleDeploy() {

        String className = SourceCodeUtils.parseClassName(codeArea.getText(), "SmartContract");
        try {
            String javaCode = SourceCodeUtils.normalizeSourceCode(codeArea.getText());
            CompilationPackage compilationPackage = new InMemoryCompiler().compile(className, javaCode);

            if (compilationPackage.isCompilationStatusSuccess()) {
                List<CompilationUnit>  compilationUnits = compilationPackage.getUnits();
                CompilationUnit compilationUnit = compilationUnits.get(0);
                byte[] byteCode = compilationUnit.getBytecode();
                String hashState = ApiUtils.generateSmartContractHashState(byteCode);
                ApiUtils.deploySmartContractProcess(javaCode, byteCode, hashState);
            } else {
                DiagnosticCollector collector = compilationPackage.getCollector();
                List<Diagnostic> diagnostics = collector.getDiagnostics();
                StringBuilder errors = new StringBuilder();
                diagnostics.forEach(action -> {
                    errors.append(String.format("line %s, error %s; ", action.getLineNumber(), action.getMessage(null)));
                });
                throw new CompilationException(
                    String.format("Compilation errors: %s", errors.toString())
                );
            }
        } catch (CompilationException | CreditsException e) {
            LOGGER.error(e.toString(), e);
            FormUtils.showError(AppState.NODE_ERROR + ": " + e.getMessage());
        }
    }

    private void checkClassAndSuperclassNames(String className, String sourceCode) throws CreditsException {
        if (!className.equals(CLASS_NAME)) {
            throw new CreditsException(String.format("Wrong class name %s, class name must be %s", className, CLASS_NAME));
        }
        String superclassName = SourceCodeUtils.parseSuperclassName(sourceCode);

        if (!superclassName.equals(SUPERCLASS_NAME)) {
            throw new CreditsException(String.format("Wrong superclass name %s, superclass name must be %s", superclassName, SUPERCLASS_NAME));
        }
    }

}