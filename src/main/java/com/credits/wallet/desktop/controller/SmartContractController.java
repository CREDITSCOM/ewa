package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.EclipseJdt;
import com.credits.wallet.desktop.utils.Utils;
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.thrift.executor.APIResponse;
import com.credits.wallet.desktop.thrift.executor.ContractExecutor;
import com.credits.wallet.desktop.thrift.executor.ContractFile;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
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
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

    private static final String[] KEYWORDS =
        new String[] {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
            "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"};

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[]]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
        "(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN +
            ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" + "|(?<STRING>" +
            STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")");

    private static final String dftCode="public class Contract extends SmartContract {\n" +
            "\n" +
            "    public Contract() {\n" +
            "        total = 0;\n" +
            "    }" +
            "\n" +
            "}";
    private static final String nonChangedStr="public class Contract extends SmartContract {";

    private static final String[] parentMethods = new String[] {
            "double total",
            "Double getBalance(String address, String currency)",
            "TransactionData getTransaction(String transactionId)",
            "List<TransactionData> getTransactions(String address, long offset, long limit)",
            "List<PoolData> getPoolList(long offset, long limit)",
            "PoolData getPool(String poolNumber)",
            "void sendTransaction(String source, String target, Double amount, String currency)"
    };

    private CodeArea codeArea;
    private TableView tabErrors;

    private String prevCode;

    @FXML
    private Pane paneCode;

    @FXML
    private TreeView<Label> classTreeView;

    @FXML
    private Button checkButton;

    //@FXML
    //private javafx.scene.control.TextArea taCode;

    @FXML
    private void handleBack() {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
            AppState.executor = null;
        }
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleDeploy() {
        char[] characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        sb.append("CST");
        Random random = new Random();
        int max = characters.length - 1;
        for (int i = 0; i < 29; i++) {
            sb.append(characters[random.nextInt(max)]);
        }

        String token = sb.toString();

        // Call contract executor
        if (AppState.contractExecutorHost != null &&
                AppState.contractExecutorPort != null) {
            try {
                // Parse className
                String className = "SmartContract";

                String javaCode = codeArea.getText().replace("\r", " ").replace("\n", " ").replace("{", " {");

                while (javaCode.contains("  ")) {
                    javaCode = javaCode.replace("  ", " ");
                }
                java.util.List<String> javaCodeWords = Arrays.asList(javaCode.split(" "));
                int ind = javaCodeWords.indexOf("class");
                if (ind >= 0 && ind < javaCodeWords.size() - 1) {
                    className = javaCodeWords.get(ind + 1);
                }
                // ---------------

                TTransport transport;

                transport = new TSocket(AppState.contractExecutorHost, AppState.contractExecutorPort);
                transport.open();

                TProtocol protocol = new TBinaryProtocol(transport);
                ContractExecutor.Client client = new ContractExecutor.Client(protocol);

                ContractFile contractFile = new ContractFile();
                contractFile.setName(className+".java");
                contractFile.setFile(codeArea.getText().getBytes());

                APIResponse executorResponse = client.store(
                        contractFile,
                        token,
                        Converter.encodeToBASE64(Ed25519.privateKeyToBytes(AppState.privateKey))
                );
                if (executorResponse.getCode()!=0 && executorResponse.getMessage()!=null) {
                    Utils.showError("Error executing smart contract " + executorResponse.getMessage());
                } else {
                    StringSelection selection = new StringSelection(token);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    Utils.showInfo("Token\n\n" + token + "\n\nhas generated and copied to clipboard");
                }

                transport.close();
            } catch (Exception e) {
                LOGGER.error("Error executing smart contract " + e.toString(), e);
                Utils.showError("Error executing smart contract " + e.toString());
            }
        }
        // ----------------------
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        prevCode=dftCode;

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.isControlDown() && ke.getCode().equals(KeyCode.SPACE)) {
                    codePopup();
                }
            }
        });

        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                    String curCode=codeArea.getText();

                    // Replace TAB to 4 spaces
                    if (curCode.indexOf("\t")>=0) {
                        codeArea.replaceText(0, curCode.length(), curCode.replace("\t", "    "));
                        curCode = codeArea.getText();
                    }

                    if (curCode.indexOf(nonChangedStr)<0) {
                        codeArea.replaceText(0, curCode.length(), prevCode);
                    } else {
                        int i1=curCode.indexOf(nonChangedStr);
                        if (curCode.indexOf(nonChangedStr,i1+1)>0) {
                            codeArea.replaceText(0, curCode.length(), prevCode);
                        }
                    }
                    prevCode=codeArea.getText();
                });

        codeArea.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
            .successionEnds(Duration.ofMillis(500))
            .supplyTask(this::computeHighlightingAsync)
            .awaitLatest(codeArea.richChanges())
            .filterMap(t -> {
                if (t.isSuccess()) {
                    return Optional.of(t.get());
                } else {
                    t.getFailure().printStackTrace();
                    return Optional.empty();
                }
            })
            .subscribe(this::applyHighlighting);

        codeArea.setPrefHeight(paneCode.getPrefHeight());
        codeArea.setPrefWidth(paneCode.getPrefWidth());
        codeArea.replaceText(0, 0, dftCode);
        paneCode.getChildren().add(codeArea);

        tabErrors = new TableView();
        tabErrors.setPrefHeight(paneCode.getPrefHeight() * 0.3);
        tabErrors.setPrefWidth(paneCode.getPrefWidth());

        TableColumn tabErrorsColLine = new TableColumn();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setPrefWidth(paneCode.getPrefWidth() * 0.1);
        TableColumn tabErrorsColText = new TableColumn();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setPrefWidth(paneCode.getPrefWidth() * 0.9);
        tabErrors.getColumns().add(tabErrorsColLine);
        tabErrors.getColumns().add(tabErrorsColText);

        TableColumn[] tableColumns = new TableColumn[tabErrors.getColumns().size()];
        for (int i = 0; i < tabErrors.getColumns().size(); i++) {
            tableColumns[i] = (TableColumn) tabErrors.getColumns().get(i);
        }
        tableColumns[0].setCellValueFactory(new PropertyValueFactory<ErrorCodeTabRow, String>("line"));
        tableColumns[1].setCellValueFactory(new PropertyValueFactory<ErrorCodeTabRow, String>("text"));

        tabErrors.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                ErrorCodeTabRow tabRow = (ErrorCodeTabRow) tabErrors.getSelectionModel().getSelectedItem();
                if (tabRow != null) {
                    positionCodeAreaToLine(Integer.valueOf(tabRow.getLine()));
                }
            }
        });

    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {

        this.classTreeView.setRoot(null);

        String sourceCode = codeArea.getText();

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
                        positionCodeAreaToLine(compilationUnit.getLineNumber(node.getStartPosition()));
                    }
                });

                TreeItem<Label> treeItem = new TreeItem<>();
                treeItem.setValue(label);

                treeRoot.getChildren().add(treeItem);
            }

        });

        root.accept(new ASTVisitor() {

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
                        positionCodeAreaToLine(compilationUnit.getLineNumber(node.getStartPosition()));
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
        }
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" : matcher.group("PAREN") != null ? "paren"
                : matcher.group("BRACE") != null ? "brace" : matcher.group("BRACKET") != null ? "bracket"
                    : matcher.group("SEMICOLON") != null ? "semicolon" : matcher.group("STRING") != null ? "string"
                        : matcher.group("COMMENT") != null ? "comment" : null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        AppState.executor.execute(task);
        return task;
    }

    private void positionCodeAreaToLine(int line) {
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

    private void codePopup() {
        ContextMenu contextMenu = new ContextMenu();

        String word="";
        int pos=codeArea.getCaretPosition()-1;
        String txt=codeArea.getText();
        while (pos>0 && !txt.substring(pos,pos+1).equals(" ") &&
                !txt.substring(pos,pos+1).equals("\r") && !txt.substring(pos,pos+1).equals("\n")) {
            word=txt.substring(pos,pos+1)+word;
            pos--;
        }

        for (String method : parentMethods) {
            if (word.trim().isEmpty() || method.toUpperCase().indexOf(word.trim().toUpperCase())>0) {
                MenuItem action = new MenuItem(method);
                contextMenu.getItems().add(action);
                action.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        int pos = codeArea.getCaretPosition();
                        String txt = codeArea.getText();
                        String txtToIns = normMethodName(method);
                        codeArea.replaceText(pos, pos, txtToIns);
                    }
                });
            }
        }

        if (contextMenu.getItems().isEmpty()) {
            MenuItem action = new MenuItem("No suggestions");
            contextMenu.getItems().add(action);
        }

        contextMenu.show(codeArea,
                codeArea.getCaretBounds().get().getMaxX(), codeArea.getCaretBounds().get().getMaxY());
    }

    private String normMethodName (String method) {
        int ind1=method.indexOf(" ");
        String result=method.substring(ind1+1);

        ind1=result.indexOf("(");
        int ind2=result.indexOf(")");
        StringBuilder parametersStr=new StringBuilder();
        String[] parameters=result.substring(ind1,ind2).trim().split(",");
        boolean first=true;
        for (String parameter : parameters) {
            String[] parameterAsArr=parameter.trim().split(" ");
            if (first)
                parametersStr.append(parameterAsArr[1].trim());
            else
                parametersStr.append(", ").append(parameterAsArr[1].trim());
            first=false;
        }

        return result.substring(0,ind1+1)+parametersStr+")";
    }
}