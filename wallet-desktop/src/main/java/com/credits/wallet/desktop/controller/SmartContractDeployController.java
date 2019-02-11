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
import com.credits.wallet.desktop.struct.TokenInfoData;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.CompilationResult;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
import static com.credits.wallet.desktop.utils.DeployControllerUtils.getTokenStandard;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.generateSmartContractAddress;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.saveSmartInTokenList;
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
    @FXML
    public VBox feeDeployPane;
    @FXML
    private DeployTabController deployTabController;
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
        deployTabController.session = session;
        deployTabController.parentController = this;
        deployTabController.initializeForm(null);
        FormUtils.initFeeField(feeField,actualOfferedMaxFeeLabel);

    }


    public void cleanCompilationPackage(boolean buildButtonDisable) {
        compilationPackage = null;
        deployButton.setDisable(true);
        buildButton.setDisable(buildButtonDisable);
    }

    @FXML
    private void handleBuild() {
        buildButton.setText(COMPILING);
        buildButton.setDisable(true);
        deployTabController.smartErrorTableView.setVisible(false);
        deployTabController.smartCodeArea.setDisable(true);
        supplyAsync(() -> SourceCodeBuilder.compileSourceCode(deployTabController.smartCodeArea.getText())).whenComplete(
            handleCallback(handleBuildResult()));
    }

    private Callback<CompilationResult> handleBuildResult() {
        return new Callback<CompilationResult>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(CompilationResult compilationResult) {
                Platform.runLater(() -> {
                    deployTabController.smartCodeArea.setDisable(false);
                    buildButton.setText(BUILD);
                });
                if(checkNotError(deployTabController.smartErrorPanel, deployTabController.smartErrorTableView,
                    compilationResult)) {
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
                    deployTabController.smartCodeArea.setDisable(false);
                    buildButton.setDisable(false);
                    buildButton.setText(BUILD);
                    FormUtils.showPlatformError(e.getMessage());
                });
                LOGGER.error("failed!", e);
            }
        };
    }

    boolean checkNotError(VBox smartErrorPanel, TableView<BuildSourceCodeError> tableView,
        CompilationResult compilationResult) {
        List<BuildSourceCodeError> errorsList = compilationResult.getErrors();
        if (errorsList.size() > 0) {
            Platform.runLater(() -> {
                buildButton.setDisable(false);
                tableView.getItems().clear();
                tableView.getItems().addAll(errorsList);
                tableView.setVisible(true);
                tableView.setPrefHeight(smartErrorPanel.getPrefHeight());
            });
            return false;
        }
        return true;
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
            String javaCode = SourceCodeUtils.normalizeSourceCode(deployTabController.smartCodeArea.getText());
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
                        (transactionData) -> createSmartContractTransaction(transactionData, FormUtils.getActualOfferedMaxFee16Bits(feeField),
                            smartContractData, session))
                        .whenComplete(
                            handleCallback(handleDeployResult(getTokenInfo(contractClass, smartContractData))));
                    loadVista(WALLET);
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
                TransactionFlowResultData transactionFlowResultData = resultData.getRight();
                String target = transactionFlowResultData.getTarget();
                StringSelection selection = new StringSelection(target);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                FormUtils.showPlatformInfo(
                    String.format("%s%n%nSmart-contract address%n%n%s%n%ncopied to clipboard",
                            transactionFlowResultData.getMessage(), target
                    )
                );
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
        loadVista(SMART_CONTRACT);
    }




    private void clearLabErr() {
        FormUtils.clearErrorOnField(feeField, feeErrorLabel);
    }


    @Override
    public void formDeinitialize() {
        deployTabController.formDeinitialize();
    }

}
