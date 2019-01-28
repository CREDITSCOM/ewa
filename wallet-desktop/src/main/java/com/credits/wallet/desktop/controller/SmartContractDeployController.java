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
import com.credits.general.util.Utils;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import com.credits.wallet.desktop.struct.TokenInfoData;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.DeployControllerUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.NumberUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.CompilationResult;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
import static com.credits.wallet.desktop.utils.DeployControllerUtils.getContractFromTemplate;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.getTokenStandard;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.refreshTreeView;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.generateSmartContractAddress;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.saveSmartInTokenList;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea.DEFAULT_SOURCE_CODE;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.BASIC_STANDARD_CLASS;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.DEFAULT_STANDARD_CLASS;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.EXTENSION_STANDARD_CLASS;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractDeployController extends AbstractController {

    public static final String BUILD = "Build";
    public static final String COMPILING = "Compiling...";
    private static final String ERR_FEE = "Fee must be greater than 0";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);

    public Pane mainPane;
    public Pane tabPanel;

    ContextMenu contextMenu = new ContextMenu();

    @FXML
    public ComboBox<String> cbContractType;

    @FXML
    public TextField deployName;

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

    @FXML
    public TextField className;


    private CreditsCodeArea codeArea;
    private short actualOfferedMaxFee16Bits;

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
    @FXML
    private TextField feeField;
    @FXML
    private Label actualOfferedMaxFeeLabel;
    @FXML
    private Label feeErrorLabel;


    public CompilationPackage compilationPackage;

    @Override
    public void initializeForm(Map<String, Object> objects) {
        initCodeArea();
        initNewContractForm();
        initDeployContractList();
        initSplitPane();
        initErrorTableView();
        feeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                newValue = NumberUtils.getCorrectNum(newValue);
                if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(newValue) && !newValue.isEmpty()) {
                    refreshOfferedMaxFeeValues(oldValue);
                    return;
                }
                refreshOfferedMaxFeeValues(newValue);
            } catch (Exception e) {
                //FormUtils.showError("Error. Reason: " + e.getMessage());
                refreshOfferedMaxFeeValues(oldValue);
            }
        });

    }

    private void initNewContractForm() {
        className.clear();
        ObservableList items = cbContractType.getItems();
        items.clear();
        items.add(DEFAULT_STANDARD_CLASS);
        items.add(BASIC_STANDARD_CLASS);
        items.add(EXTENSION_STANDARD_CLASS);
        cbContractType.getSelectionModel().select(0);

    }

    private void initCodeArea() {
        codeArea = CodeAreaUtils.initCodeArea(paneCode, false);
        refreshTreeView(treeView, codeArea);
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (evt) -> {
            refreshTreeView(treeView, codeArea);
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
        ArrayList<DeploySmartListItem> deploySmartListItems =
            session.deployContractsKeeper.getKeptObject().orElseGet(ArrayList::new);


        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN);

        deployContractList.setOnKeyPressed(event -> {
            if (keyCombinationShiftC.match(event)) {
                deployContractList.getItems().remove(getCurrentListItem());
                session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
                if (deployContractList.getItems().size() == 0) {
                    handleAddContract();
                }
            }
        });

        deployContractList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Platform.runLater(() -> {
                    contextMenu.getItems().clear();
                    contextMenu.hide();
                    MenuItem removeItem = new MenuItem("Delete");
                    contextMenu.getItems().add(removeItem);

                    removeItem.setOnAction(event1 -> {
                        deployContractList.getItems().remove(getCurrentListItem());
                        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
                        if (deployContractList.getItems().size() == 0) {
                            handleAddContract();
                        }
                    });
                    contextMenu.show(deployContractList, event.getScreenX(), event.getScreenY());
                });
            }
        });

        deployContractList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            session.lastSmartIndex = deployContractList.getSelectionModel().getSelectedIndex();
            if (oldValue != null) {
                oldValue.sourceCode = codeArea.getText();
            }
            if (newValue.state == DeploySmartListItem.ItemState.NEW) {
                deleteMainTabs();
            } else {
                DeploySmartListItem item = getCurrentListItem();
                returnMainTabs(item);
                refreshTreeView(treeView, codeArea);
            }
        });

        if (deploySmartListItems.isEmpty()) {
            DeploySmartListItem deploySmartItem = new DeploySmartListItem(DEFAULT_SOURCE_CODE,
                DeployControllerUtils.checkContractNameExist("Contract", deployContractList.getItems()),
                DeploySmartListItem.ItemState.SAVED);
            deployContractList.getItems().add(deploySmartItem);
            deployContractList.getSelectionModel().selectFirst();
            session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        } else {
            deployContractList.getItems().addAll(deploySmartListItems);
            deployContractList.getSelectionModel().select(session.lastSmartIndex);
        }

        if (getCurrentListItem().state.equals(DeploySmartListItem.ItemState.NEW)) {
            deleteMainTabs();
        } else {
            returnMainTabs(getCurrentListItem());
        }
        tabPane.getSelectionModel().select(0);
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
        // VALIDATE
        AtomicBoolean isValidationSuccessful = new AtomicBoolean(true);
        clearLabErr();
        String transactionFee = feeField.getText();
        if (GeneralConverter.toBigDecimal(transactionFee).compareTo(BigDecimal.ZERO) <= 0) {
            FormUtils.validateField(feeField, feeErrorLabel, ERR_FEE, isValidationSuccessful);
        }
        if (!isValidationSuccessful.get()) {
            return;
        }
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
                        (transactionData) -> createSmartContractTransaction(transactionData, actualOfferedMaxFee16Bits,
                            smartContractData, session))
                        .whenComplete(
                            handleCallback(handleDeployResult(getTokenInfo(contractClass, smartContractData))));
                    loadVista(WALLET, this);
                }
            }
        } catch (Exception e) {
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
                    saveSmartInTokenList(session.coinsKeeper, tokenInfoData.name, tokenInfoData.balance,
                        tokenInfoData.address);
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

    private void clearLabErr() {
        FormUtils.clearErrorOnField(feeField, feeErrorLabel);
    }

    private void refreshOfferedMaxFeeValues(String oldValue) {
        if (oldValue.isEmpty()) {
            actualOfferedMaxFeeLabel.setText("");
            feeField.setText("");
        } else {
            Pair<Double, Short> actualOfferedMaxFeePair =
                Utils.createActualOfferedMaxFee(GeneralConverter.toDouble(oldValue));
            this.actualOfferedMaxFeeLabel.setText(GeneralConverter.toString(actualOfferedMaxFeePair.getLeft()));
            this.actualOfferedMaxFee16Bits = actualOfferedMaxFeePair.getRight();
            feeField.setText(oldValue);
        }
    }

    @Override
    public void formDeinitialize() {
        DeploySmartListItem item = getCurrentListItem();
        item.sourceCode = codeArea.getText();
        codeArea.cleanAll();
    }


    private void saveTypeOfContract(DeploySmartListItem item) {
        item.state = DeploySmartListItem.ItemState.SAVED;
        returnMainTabs(item);
    }

    private void deleteMainTabs() {
        cleanCompilationPackage(true);
        tabPane.getTabs().removeAll(createCodeTab, codeAreaTab, testingTab);
        tabPane.getTabs().add(createCodeTab);
        tabPane.getSelectionModel().select(0);
    }

    private void returnMainTabs(DeploySmartListItem item) {
        cleanCompilationPackage(false);
        tabPane.getTabs().removeAll(createCodeTab, codeAreaTab, testingTab);
        codeAreaTab.setText(item.name);
        tabPane.getTabs().add(codeAreaTab);
        tabPane.getTabs().add(testingTab);
        tabPane.getSelectionModel().select(0);
        codeArea.replaceText(item.sourceCode);
        refreshTreeView(treeView, codeArea);
    }

    private DeploySmartListItem getCurrentListItem() {
        return deployContractList.getSelectionModel().getSelectedItem();
    }

    public void handleAddContract() {
        DeploySmartListItem deploySmartItem = new DeploySmartListItem(null,
            DeployControllerUtils.checkContractNameExist("NewContract", deployContractList.getItems()),
            DeploySmartListItem.ItemState.NEW);
        deployContractList.getItems().add(deploySmartItem);
        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        deployContractList.getSelectionModel().selectLast();
        deleteMainTabs();
    }

    @FXML
    public void handleGenerateSmart() {
        String selectedType = cbContractType.getSelectionModel().getSelectedItem();
        String curClassName;
        if (className.getText().isEmpty()) {
            curClassName = "Contract";
        } else {
            curClassName = className.getText();

        if ((!SourceVersion.isIdentifier(curClassName) && !SourceVersion.isKeyword(curClassName)) || !curClassName.matches("^[a-zA-Z0-9]+$")) {
                FormUtils.showInfo("ClassName is not valid");
                return;
            }
        }
        try {
            String contractFromTemplate = getContractFromTemplate(selectedType);
            String sourceCode = null;
            if (contractFromTemplate != null) {
                sourceCode = String.format(contractFromTemplate, curClassName, curClassName);
            }
            DeploySmartListItem item = getCurrentListItem();
            item.sourceCode = sourceCode;
            item.name = DeployControllerUtils.checkContractNameExist(curClassName, deployContractList.getItems());
            saveTypeOfContract(item);
            deployContractList.refresh();
            session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
            initNewContractForm();
        } catch (Exception e) {
            DeploySmartListItem currentListItem = getCurrentListItem();
            currentListItem.sourceCode= DEFAULT_SOURCE_CODE;
            saveTypeOfContract(currentListItem);
        }
    }
}
