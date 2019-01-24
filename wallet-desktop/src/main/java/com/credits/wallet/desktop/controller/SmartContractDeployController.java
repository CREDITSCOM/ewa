package com.credits.wallet.desktop.controller;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TokenStandartData;
import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.util.ByteArrayContractClassLoader;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import com.credits.wallet.desktop.struct.TokenInfoData;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.CompilationResult;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.client.node.util.TransactionIdCalculateUtils.getCalcTransactionIdSourceTargetResult;
import static com.credits.client.node.util.TransactionIdCalculateUtils.getIdWithoutFirstTwoBits;
import static com.credits.general.util.GeneralConverter.decodeFromBASE58;
import static com.credits.general.util.GeneralConverter.encodeToBASE58;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.VistaNavigator.SMART_CONTRACT;
import static com.credits.wallet.desktop.VistaNavigator.WALLET;
import static com.credits.wallet.desktop.VistaNavigator.loadVista;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.getTokenStandard;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.refreshTreeView;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.generateSmartContractAddress;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.saveSmartInTokenList;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea.BASIC_SOURCE_CODE;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea.DEFAULT_SOURCE_CODE;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea.EXTENDED_SOURCE_CODE;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractDeployController extends AbstractController {

    public static final String BUILD = "Build";
    public static final String COMPILING = "Compiling...";
    public static final int HEADER_HEIGHT = 30;
    public static final int TABLE_LINE_HEIGHT = 25;
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);
    public Pane mainPane;
    public Pane tabPanel;

    @FXML
    public ListView<DeploySmartListItem> deployContractList;

    @FXML
    public TabPane tabPane;

    @FXML
    public Tab testingTab;

    @FXML
    public Tab codeAreaTab;

    @FXML
    public Tab createCodeTab;


    private CreditsCodeArea codeArea;

    @FXML
    public Pane buttonPane;

    @FXML
    private TableView<BuildSourceCodeError> errorTableView;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Pane paneCode;

    @FXML
    private Pane debugPane;

    @FXML
    private TreeView<Label> treeView;

    @FXML
    private Button deployButton;

    @FXML
    private Button buildButton;


    public CompilationPackage compilationPackage;

    @Override
    public void initializeForm(Map<String, Object> objects) {

        initCodeArea();
        initDeployContractList();
        initSplitPane();
        initErrorTableView();
    }

    private void initCodeArea() {
        codeArea = CodeAreaUtils.initCodeArea(paneCode, false);
        refreshTreeView(treeView,codeArea);
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (evt) -> {
            refreshTreeView(treeView,codeArea);
            cleanCompilationPackage(false);
        });
    }

    private void initSplitPane() {
        for (SplitPane.Divider d : splitPane.getDividers()) {
            d.positionProperty()
                .addListener((observable, oldValue, newValue) -> errorTableView.setPrefHeight(debugPane.getHeight()));
        }
    }

    private void initDeployContractList() {
        if (session.deploySmartListItems == null) {
            DeploySmartListItem deploySmartItem =
                new DeploySmartListItem(DEFAULT_SOURCE_CODE, "SmartContract " + session.lastSmartIndex++,
                    DeploySmartListItem.ItemState.SAVED);
            deployContractList.getItems().add(deploySmartItem);
            deployContractList.getSelectionModel().selectFirst();
            session.deploySmartListItems = deployContractList.getItems();
        } else {
            deployContractList.getSelectionModel().selectFirst();
            deployContractList.getItems().addAll(session.deploySmartListItems);
        }

        tabPane.getTabs().remove(createCodeTab);
        tabPane.getSelectionModel().select(0);

        deployContractList.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                if(oldValue!=null) {
                    oldValue.sourceCode = codeArea.getText();
                }
                if(newValue.state == DeploySmartListItem.ItemState.NEW) {
                    deleteMainTabs();
                } else {
                    DeploySmartListItem item = getCurrentListItem();
                    returnMainTabs(item);
                }
            });
    }

    private void cleanCompilationPackage(boolean buildButtonDisable) {
        compilationPackage = null;
        deployButton.setDisable(true);
        buildButton.setDisable(buildButtonDisable);
    }

    @FXML
    private void handleBuild() {
        buildButton.setText(COMPILING);
        buildButton.setDisable(true);
        errorTableView.setVisible(false);
        codeArea.setDisable(true);
        supplyAsync(() -> SourceCodeBuilder.compileSourceCode(codeArea.getText())).whenComplete(
            handleCallback(handleBuildResult()));
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
                        errorTableView.getItems().clear();
                        errorTableView.getItems().addAll(errorsList);
                        errorTableView.setVisible(true);
                        errorTableView.setPrefHeight(debugPane.getPrefHeight());
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
                    List<ByteCodeObjectData> byteCodeObjectDataList =
                        GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);

                    Class<?> contractClass = compileSmartContractByteCode(byteCodeObjectDataList);
                    TokenStandartData tokenStandartData = getTokenStandard(contractClass);

                    SmartContractDeployData smartContractDeployData =
                        new SmartContractDeployData(javaCode, byteCodeObjectDataList, tokenStandartData);

                    long idWithoutFirstTwoBits = getIdWithoutFirstTwoBits(nodeApiService, session.account, true);

                    SmartContractData smartContractData = new SmartContractData(
                        generateSmartContractAddress(decodeFromBASE58(session.account), idWithoutFirstTwoBits,
                            byteCodeObjectDataList), decodeFromBASE58(session.account), smartContractDeployData, null);

                    supplyAsync(() -> getCalcTransactionIdSourceTargetResult(nodeApiService, session.account,
                        smartContractData.getBase58Address(), idWithoutFirstTwoBits), threadPool).thenApply(
                        (transactionData) -> createSmartContractTransaction(transactionData, smartContractData,
                            session))
                        .whenComplete(
                            handleCallback(handleDeployResult(getTokenInfo(contractClass, smartContractData))));
                    session.lastSmartContract = codeArea.getText();

                    loadVista(WALLET, this);
                }
            }
        } catch (CreditsException e) {
            LOGGER.error("failed!", e);
            FormUtils.showError(NODE_ERROR + ": " + e.getMessage());
        }
    }

    private TokenInfoData getTokenInfo(Class<?> contractClass, SmartContractData smartContractData) {
        if (smartContractData.getSmartContractDeployData().getTokenStandardData() != TokenStandartData.NotAToken) {
            try {
                Object contractInstance = contractClass.getDeclaredConstructor(String.class)
                    .newInstance(encodeToBASE58(smartContractData.getDeployer()));
                Field initiator = contractClass.getSuperclass().getDeclaredField("initiator");
                initiator.setAccessible(true);
                initiator.set(contractInstance, session.account);
                String tokenName = (String) contractClass.getMethod("getName").invoke(contractInstance);
                String balance = (String) contractClass.getMethod("balanceOf", String.class)
                    .invoke(contractInstance, session.account);
                return new TokenInfoData(smartContractData.getBase58Address(), tokenName, new BigDecimal(balance));
            } catch (Exception e) {
                LOGGER.warn("token \"{}\" can't be add to the balances list. Reason: {}",
                    smartContractData.getBase58Address(), e.getMessage());
            }
        }
        return null;
    }

    private static Class<?> compileSmartContractByteCode(List<ByteCodeObjectData> smartContractByteCodeData) {
        ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();
        Class<?> contractClass = null;
        for (ByteCodeObjectData compilationUnit : smartContractByteCodeData) {
            Class<?> tempContractClass =
                classLoader.buildClass(compilationUnit.getName(), compilationUnit.getByteCode());
            if (!compilationUnit.getName().contains("$")) {
                contractClass = tempContractClass;
            }
        }
        return contractClass;
    }


    private Callback<Pair<Long, TransactionFlowResultData>> handleDeployResult(TokenInfoData tokenInfoData) {
        return new Callback<Pair<Long, TransactionFlowResultData>>() {
            @Override
            public void onSuccess(Pair<Long, TransactionFlowResultData> resultData) {
                ApiUtils.saveTransactionRoundNumberIntoMap(resultData.getRight().getRoundNumber(), resultData.getLeft(),
                    session);
                String target = resultData.getRight().getTarget();
                StringSelection selection = new StringSelection(target);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                FormUtils.showPlatformInfo(
                    String.format("Smart-contract address%n%n%s%n%nhas generated and copied to clipboard", target));
                if (tokenInfoData != null) {
                    saveSmartInTokenList(session.coinsKeeper, tokenInfoData.name, tokenInfoData.balance, tokenInfoData.address);
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
            if (event.isPrimaryButtonDown() || event.getButton() == MouseButton.PRIMARY) {
                BuildSourceCodeError tabRow = errorTableView.getSelectionModel().getSelectedItem();
                try {
                    codeArea.setCaretPositionOnLine(tabRow.getLine());
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void formDeinitialize() {
        codeArea.cleanAll();
    }


    public void handleExtended() {
        saveTypeOfContract(EXTENDED_SOURCE_CODE);
    }

    public void handleBasic() {
        saveTypeOfContract(BASIC_SOURCE_CODE);
    }

    public void handleSimpleType() {
        saveTypeOfContract(DEFAULT_SOURCE_CODE);
    }

    private void saveTypeOfContract(String sourceCode) {
        DeploySmartListItem item = getCurrentListItem();
        item.sourceCode = sourceCode;
        item.state = DeploySmartListItem.ItemState.SAVED;
        returnMainTabs(item);
    }

    private void deleteMainTabs() {
        cleanCompilationPackage(true);
        tabPane.getTabs().removeAll(createCodeTab,codeAreaTab,testingTab);
        tabPane.getTabs().add(createCodeTab);
        tabPane.getSelectionModel().select(0);
    }

    private void returnMainTabs(DeploySmartListItem item) {
        cleanCompilationPackage(false);
        tabPane.getTabs().removeAll(createCodeTab,codeAreaTab,testingTab);
        tabPane.getTabs().add(codeAreaTab);
        tabPane.getTabs().add(testingTab);
        tabPane.getSelectionModel().select(0);
        codeArea.replaceText(item.sourceCode);
    }

    private DeploySmartListItem getCurrentListItem() {
        return deployContractList.getSelectionModel().getSelectedItem();
    }

    public void handleAddContract() {
        DeploySmartListItem deploySmartItem =
            new DeploySmartListItem(null, "SmartContract " + session.lastSmartIndex++, DeploySmartListItem.ItemState.NEW);
        deployContractList.getItems().add(deploySmartItem);
        session.deploySmartListItems = deployContractList.getItems();
        deployContractList.getSelectionModel().selectLast();
        deleteMainTabs();
    }






}
