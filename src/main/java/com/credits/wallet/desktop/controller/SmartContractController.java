package com.credits.wallet.desktop.controller;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
<<<<<<< HEAD
import com.credits.wallet.desktop.service.DebugService;
import com.credits.wallet.desktop.utils.EclipseJdt;
import com.credits.wallet.desktop.utils.Utils;
=======
>>>>>>> origin/smart-contract-v2
import com.credits.wallet.desktop.struct.ErrorCodeTabRow;
import com.credits.wallet.desktop.thrift.executor.APIResponse;
import com.credits.wallet.desktop.thrift.executor.ContractExecutor;
import com.credits.wallet.desktop.thrift.executor.ContractFile;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.EclipseJdt;
import com.credits.wallet.desktop.utils.SimpleInMemoryCompilator;
import com.credits.wallet.desktop.utils.Utils;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
<<<<<<< HEAD
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
=======
import javafx.scene.control.Button;
>>>>>>> origin/smart-contract-v2
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

<<<<<<< HEAD
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

    private List<String> breakPoints=new ArrayList<>();
    private int dbgCursor;
    private boolean dbgMode;
    private DebugService dbgService;


=======
>>>>>>> origin/smart-contract-v2
    @FXML
    Label address;

    @FXML
    private TextField txAddress;

    @FXML
    private TreeView<Label> contractsTree;

    @FXML
    private Button dbgDebugButton;
    @FXML
    private Button dbgStopButton;
    @FXML
    private Button dbgStepButton;
    @FXML
    private Button dbgGoButton;
    @FXML
    private Button dbgWatchButton;
    @FXML
    private TextField txDbgWatch;

    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
<<<<<<< HEAD
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
                TTransport transport;
=======
    private void handleCreate() {
        App.showForm("/fxml/smart_contract_deploy.fxml", "Wallet");
    }
>>>>>>> origin/smart-contract-v2

    @FXML
    private void handleSearch() {
        String address = txAddress.getText();
        try {
            SmartContractData smartContractData = AppState.apiClient.getSmartContract(address);


<<<<<<< HEAD
                ContractFile contractFile = new ContractFile();
                contractFile.setName(parseClassName()+".java");
                contractFile.setFile(codeArea.getText().getBytes());
=======
>>>>>>> origin/smart-contract-v2

            // this.address.setText(smartContractData.getAddress);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            Utils.showError(String.format("Error %s", e.getMessage()));
        }
    }

<<<<<<< HEAD
    @FXML
    private void handleDbgDebug() {
        dbgMode = false;
        if (breakPoints.size()==0) {
            Utils.showError("Please set one or more breakpoints");
        } else {
            String className=parseClassName();
            dbgService=new DebugService(parseClassName(), codeArea.getText());
            String compileError=dbgService.compile();
            if (!compileError.isEmpty())
                Utils.showError(compileError);
            else {
                String startError=dbgService.start();
                if (!startError.isEmpty())
                    Utils.showError(startError);
                else {
                    for (String lineNumber : breakPoints) {
                        int realLineNumber=Integer.valueOf(lineNumber)+1;
                        String cmd="stop at "+className+":"+realLineNumber;
                        dbgService.execCmd(cmd);
                    }
                    dbgService.execCmd("run");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    dbgService.execCmd("");

                    dbgMode = true;
                    dbgSetCursor();
                }
            }
        }
        showDbgButtons();
    }

    @FXML
    private void handleDbgStop() {
        dbgService.destroy();
        dbgMode=false;
        showDbgButtons();
        repaintCodeArea();
        Utils.showInfo("Dubug has finished");
    }

    @FXML
    private void handleDbgStep() throws Exception {
        dbgService.execCmd("step");
        Thread.sleep(1000);
        dbgService.execCmd("");
        dbgSetCursor();
    }

    @FXML
    private void handleDbgGo() throws Exception {
        dbgService.execCmd("cont");
        Thread.sleep(1000);
        dbgService.execCmd("");
        dbgSetCursor();
    }

    @FXML
    private void handleDbgWatch() throws Exception {
        dbgWatchExp(txDbgWatch.getText());
    }
=======
>>>>>>> origin/smart-contract-v2

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
<<<<<<< HEAD
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
        AppState.executor = Executors.newSingleThreadExecutor();

        prevCode=dftCode;

        codeArea = new CodeArea();

        IntFunction<Node> lineNumberFunction = new IntFunction<Node>() {
            @Override
            public Node apply(int value) {
                Label lineNo = new Label();
                lineNo.setFont(Font.font("monospace", FontPosture.ITALIC, 13));

                if (dbgMode && value==dbgCursor-1)
                    lineNo.setBackground(new Background(new BackgroundFill(Color.web("blue"), null, null)));
                else if (breakPoints.contains(Integer.toString(value)))
                    lineNo.setBackground(new Background(new BackgroundFill(Color.web("red"), null, null)));
                else
                    lineNo.setBackground(new Background(new BackgroundFill(Color.web("#ddd"), null, null)));

                lineNo.setTextFill(Color.web("#666"));
                lineNo.setPadding(new Insets(0.0, 5.0, 0.0, 5.0));
                lineNo.setAlignment(Pos.TOP_RIGHT);
                lineNo.getStyleClass().add("lineno");

                String result=Integer.toString(value+1);
                while (result.length()<3)
                    result=" "+result;
                lineNo.setText(result);
                return lineNo;
            }
        };

        codeArea.setParagraphGraphicFactory(lineNumberFunction);

        codeArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {
                if (ke.isControlDown() && ke.getCode().equals(KeyCode.SPACE)) {
                    codePopup();
                }
            }
        });

        codeArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount()==2) {
                    int lineNum=codeArea.getCurrentParagraph();
                    String lineNumStr=Integer.toString(lineNum);
                    if (breakPoints.contains(lineNumStr))
                        breakPoints.remove(lineNumStr);
                    else
                        breakPoints.add(lineNumStr);

                    repaintCodeArea();
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

        dbgMode=false;
        dbgCursor=-1;
        showDbgButtons();
=======
        this.contractsTree.setRoot(null);
>>>>>>> origin/smart-contract-v2
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {
    }

    private void showDbgButtons() {
        dbgDebugButton.setVisible(!dbgMode);
        dbgStopButton.setVisible(dbgMode);
        dbgStepButton.setVisible(dbgMode);
        dbgGoButton.setVisible(dbgMode);
        dbgWatchButton.setVisible(dbgMode);
        txDbgWatch.setVisible(dbgMode);
    }

    private String parseClassName() {
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
        return className;
    }

    private void dbgSetCursor() {
        dbgCursor=-1;
        if (dbgMode) {
            Integer cursorPosition=dbgService.cursorPosition();
            if (cursorPosition!=null)
                dbgCursor=cursorPosition;
            else
                handleDbgStop();
        }
        repaintCodeArea();
    }

    private void repaintCodeArea() {
        int caretPosition=codeArea.getCaretPosition();
        String txt=codeArea.getText();
        codeArea.replaceText(0, txt.length(), "");
        codeArea.replaceText(0, txt.length(), txt);
        codeArea.displaceCaret(caretPosition);
    }

    private void dbgWatchExp(String exp) throws Exception {
        String res=dbgService.execCmd("print "+exp);
        Thread.sleep(1000);
        res=res+dbgService.execCmd("");
        Utils.showInfo(res);
    }
}