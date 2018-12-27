package com.credits.wallet.desktop.controller;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.client.node.thrift.generated.TokenStandart;
import com.credits.general.exception.CreditsException;
import com.credits.general.util.ByteArrayContractClassLoader;
import com.credits.general.util.Callback;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.general.util.compiler.model.CompilationUnit;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.CompilationResult;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.client.node.thrift.generated.TokenStandart.CreditsBasic;
import static com.credits.client.node.thrift.generated.TokenStandart.CreditsExtended;
import static com.credits.client.node.thrift.generated.TokenStandart.NotAToken;
import static com.credits.client.node.util.TransactionIdCalculateUtils.getCalcTransactionIdSourceTargetResult;
import static com.credits.client.node.util.TransactionIdCalculateUtils.getIdWithoutFirstTwoBits;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.lastSmartContract;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.VistaNavigator.SMART_CONTRACT;
import static com.credits.wallet.desktop.VistaNavigator.WALLET;
import static com.credits.wallet.desktop.VistaNavigator.loadVista;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.*;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.generateSmartContractAddress;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractDeployController implements Initializable {

    public static final String BUILD = "Build";
    public static final String COMPILING = "Compiling...";
    public static final int HEADER_HEIGHT = 30;
    public static final int TABLE_LINE_HEIGHT = 25;
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);

    private CreditsCodeArea codeArea;


    @FXML
    private TableView<BuildSourceCodeError> errorTableView;

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
    private Button deployButton;

    @FXML
    private Button buildButton;


    public CompilationPackage compilationPackage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        FormUtils.resizeForm(bp);

        codeArea = CodeAreaUtils.initCodeArea(paneCode, false);

        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (evt) -> {
            compilationPackage = null;
            deployButton.setDisable(true);
            buildButton.setDisable(false);
        });

        for (SplitPane.Divider d : splitPane.getDividers()) {
            d.positionProperty()
                .addListener((observable, oldValue, newValue) -> errorTableView.setPrefHeight(debugPane.getHeight()));
        }

        panelCodeKeyReleased();
        initErrorTableView();
    }

    @FXML
    private void handleBuild() {
        buildButton.setText(COMPILING);
        buildButton.setDisable(true);
        errorTableView.setVisible(false);
        codeArea.setDisable(true);
        supplyAsync(() -> SourceCodeBuilder.compileSourceCode(codeArea.getText()))
            .whenComplete(handleCallback(handleBuildResult()));
    }

    private Callback<CompilationResult> handleBuildResult() {
        return new Callback<CompilationResult>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(CompilationResult compilationResult) {
                List errorsList = compilationResult.getErrors();
                Platform.runLater(() -> {
                    codeArea.setDisable(false);
                    buildButton.setText(BUILD);
                });

                if (errorsList.size() > 0) {
                    Platform.runLater(() -> {
                        buildButton.setDisable(false);
                        refillErrorsTableView(errorsList);
                    });
                } else {
                    compilationPackage = compilationResult.getCompilationPackage();
                    Platform.runLater(() -> {
                        buildButton.setDisable(true);
                        deployButton.setDisable(false);
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(() -> {
                    codeArea.setDisable(false);
                    buildButton.setDisable(false);
                    buildButton.setText(BUILD);
                    FormUtils.showPlatformError(e.getMessage());
                });
                LOGGER.error("failed!", e);
            }
        };
    }

    @FXML
    private void handleDeploy() {
        try {
            String javaCode = SourceCodeUtils.normalizeSourceCode(codeArea.getText());
            if (compilationPackage == null) {
                buildButton.setDisable(false);
                deployButton.setDisable(true);
                throw new CreditsException("Source code is not compiled");
            } else {
                if (compilationPackage.isCompilationStatusSuccess()) {
                    List<CompilationUnit> compilationUnits = compilationPackage.getUnits();
                    CompilationUnit compilationUnit = compilationUnits.get(0);
                    byte[] byteCode = compilationUnit.getBytecode();

                    Class<?> contractClass = getContractClass(compilationUnit);
                    TokenStandart tokenStandart = getTokenStandard(contractClass);

                    SmartContractDeployData smartContractDeployData =
                        new SmartContractDeployData(javaCode, byteCode, tokenStandart);

                    long idWithoutFirstTwoBits = getIdWithoutFirstTwoBits(nodeApiService, account, true);

                    SmartContractData smartContractData = new SmartContractData(
                        generateSmartContractAddress(decodeFromBASE58(account), idWithoutFirstTwoBits, byteCode),
                        decodeFromBASE58(account), smartContractDeployData, null);

                    supplyAsync(() -> getCalcTransactionIdSourceTargetResult(nodeApiService,
                            account, smartContractData.getBase58Address(), idWithoutFirstTwoBits), threadPool)
                        .thenApply((transactionData) -> createSmartContractTransaction(transactionData, smartContractData))
                        .whenComplete(handleCallback(handleDeployResult(getTokenInfo(contractClass, smartContractData))));
                    lastSmartContract = codeArea.getText();

                    loadVista(WALLET, this);
                }
            }
        } catch (CreditsException e) {
            LOGGER.error("failed!", e);
            FormUtils.showError(NODE_ERROR + ": " + e.getMessage());
        }
    }

    private TokenInfo getTokenInfo(Class<?> contractClass, SmartContractData smartContractData) {
        if(smartContractData.getSmartContractDeployData().getTokenStandard() != NotAToken) {
            try {
                Object contractInstance = contractClass.getDeclaredConstructor(String.class).newInstance(encodeToBASE58(smartContractData.getDeployer()));
                Field initiator = contractClass.getSuperclass().getDeclaredField("initiator");
                initiator.setAccessible(true);
                initiator.set(contractInstance, account);
                String tokenName = (String) contractClass.getMethod("getName").invoke(contractInstance);
                String balance =
                    (String) contractClass.getMethod("balanceOf", String.class).invoke(contractInstance, account);
                return new TokenInfo(smartContractData.getBase58Address(), tokenName, new BigDecimal(balance));
            } catch (Exception e) {
                LOGGER.warn("token \"{}\" can't be add to the balances list. Reason: {}", smartContractData.getBase58Address(), e.getMessage());
            }
        }
        return null;
    }

    private Class<?> getContractClass(CompilationUnit compilationUnit) {
        return new ByteArrayContractClassLoader().buildClass(compilationUnit.getName(), compilationUnit.getBytecode());
    }

    private TokenStandart getTokenStandard(Class<?> contractClass) {
        TokenStandart tokenStandart = NotAToken;
        try {
            Class<?>[] interfaces = contractClass.getInterfaces();
            if(interfaces.length > 0) {
                Class<?> basicStandard = Class.forName("BasicStandard");
                Class<?> extendedStandard = Class.forName("ExtensionStandard");
                for (Class<?> _interface : interfaces) {
                    if (_interface.equals(basicStandard)) tokenStandart = CreditsBasic;
                    if (_interface.equals(extendedStandard)) tokenStandart = CreditsExtended;
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.debug("can't find standard classes. Reason {}", e.getMessage());
        }
        return tokenStandart;
    }

    private Callback<Pair<Long, TransactionFlowResultData>> handleDeployResult(TokenInfo tokenInfo) {
        return new Callback<Pair<Long, TransactionFlowResultData>>() {
            @Override
            public void onSuccess(Pair<Long, TransactionFlowResultData> resultData) {
                ApiUtils.saveTransactionRoundNumberIntoMap(resultData.getRight().getRoundNumber(),
                    resultData.getLeft());
                String target = resultData.getRight().getTarget();
                StringSelection selection = new StringSelection(target);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                FormUtils.showPlatformInfo(
                    String.format("Smart-contract address%n%n%s%n%nhas generated and copied to clipboard", target));
                if(tokenInfo != null){
                    saveSmartInTokenList(tokenInfo.name, tokenInfo.balance, tokenInfo.address);
                }
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("failed!", e);
                FormUtils.showPlatformError(e.getMessage());
            }
        };
    }

    @FXML
    private void handleBack() {
        loadVista(SMART_CONTRACT, this);
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
            String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
            Label labelRoot = new Label(className);
            TreeItem<Label> treeRoot = new TreeItem<>(labelRoot);

            List<FieldDeclaration> fields = ParseCodeUtils.parseFields(sourceCode);
            List<MethodDeclaration> constructors = ParseCodeUtils.parseConstructors(sourceCode);
            List<MethodDeclaration> methods = ParseCodeUtils.parseMethods(sourceCode);

            List<BodyDeclaration> classMembers = new ArrayList<>();
            classMembers.addAll(fields);
            classMembers.addAll(constructors);
            classMembers.addAll(methods);

            classMembers.forEach(classMember -> {
                if (classMember instanceof MethodDeclaration) {
                    ((MethodDeclaration) classMember).setBody(null);
                }
                Label label = new Label(classMember.toString());
                TreeItem<Label> treeItem = new TreeItem<>();
                treeItem.setValue(label);
                treeRoot.getChildren().add(treeItem);
            });

            treeRoot.setExpanded(true);
            classTreeView.setRoot(treeRoot);
            classTreeView.setShowRoot(false);

            classTreeView.setOnMouseClicked(event -> {
                if (event.isPrimaryButtonDown() || event.getButton() == MouseButton.PRIMARY) {
                    BodyDeclaration selected =
                        classMembers.get(classTreeView.getSelectionModel().getSelectedIndices().get(0));
                    try {
                        int lineNumber = ParseCodeUtils.getLineNumber(sourceCode, selected);
                        codeArea.setCaretPositionOnLine(lineNumber);
                    } catch (Exception ignored) {
                    }
                }
            });

        });
    }

    private void refillErrorsTableView(List<BuildSourceCodeError> listOfError) {
        errorTableView.getItems().clear();
        errorTableView.getItems().addAll(listOfError);
        errorTableView.setVisible(true);
        errorTableView.setPrefHeight(HEADER_HEIGHT + errorTableView.getItems().size() * TABLE_LINE_HEIGHT);
    }

    private void initErrorTableView() {
        TableColumn<BuildSourceCodeError, String> tabErrorsColLine = new TableColumn<>();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setCellValueFactory(new PropertyValueFactory<>("line"));
        tabErrorsColLine.setPrefWidth(debugPane.getPrefWidth() * 0.1);

        TableColumn<BuildSourceCodeError, String> tabErrorsColText = new TableColumn<>();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setCellValueFactory(new PropertyValueFactory<>("text"));
        tabErrorsColText.setPrefWidth(debugPane.getPrefWidth() * 0.88);

        errorTableView.setVisible(false);
        errorTableView.setPrefHeight(debugPane.getPrefHeight());
        errorTableView.setPrefWidth(debugPane.getPrefWidth());

        errorTableView.getColumns().add(tabErrorsColLine);
        errorTableView.getColumns().add(tabErrorsColText);

        errorTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()|| event.getButton() == MouseButton.PRIMARY) {
                BuildSourceCodeError tabRow = errorTableView.getSelectionModel().getSelectedItem();
                try {
                    codeArea.setCaretPositionOnLine(tabRow.getLine());
                } catch (Exception ignored) {
                }
            }
        });
    }

    private static class TokenInfo {
        final String address;
        final String name;
        final BigDecimal balance;

        private TokenInfo(String smartContractAddress, String tokenName, BigDecimal balance) {
            this.address = smartContractAddress;
            this.name = tokenName;
            this.balance = balance;
        }
    }
}