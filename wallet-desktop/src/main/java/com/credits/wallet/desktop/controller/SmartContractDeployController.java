package com.credits.wallet.desktop.controller;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TokenStandartData;
import com.credits.client.node.pojo.TransactionFlowResultData;
import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.util.Callback;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.struct.TokenInfoData;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.SourceCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.CompilationResult;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
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
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.client.node.util.TransactionIdCalculateUtils.getCalcTransactionIdSourceTargetResult;
import static com.credits.client.node.util.TransactionIdCalculateUtils.getIdWithoutFirstTwoBits;
import static com.credits.general.util.GeneralConverter.*;
import static com.credits.general.util.Utils.threadPool;
import static com.credits.wallet.desktop.AppState.NODE_ERROR;
import static com.credits.wallet.desktop.VistaNavigator.*;
import static com.credits.wallet.desktop.utils.ApiUtils.createSmartContractTransaction;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.getTokenStandard;
import static com.credits.wallet.desktop.utils.SmartContractsUtils.*;
import static com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder.compileSmartSourceCode;
import static java.util.concurrent.CompletableFuture.supplyAsync;


public class SmartContractDeployController extends AbstractController {

    public static final String BUILD = "Build";
    public static final String COMPILING = "Compiling...";
    private static final String ERR_FEE = "Fee must be greater than 0";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractDeployController.class);
    @FXML
    private DeployTabController deployTabController;

    public Pane mainPane;
    public CompilationPackage compilationPackage;

    @Override
    public void initializeForm(Map<String, Object> objects) {
        deployTabController.session = session;
        deployTabController.parentController = this;
        deployTabController.initializeForm(null);
    }


    public void cleanCompilationPackage(boolean buildButtonDisable) {
        compilationPackage = null;
        deployTabController.smartDeployButton.setDisable(true);
        deployTabController.smartBuildButton.setDisable(buildButtonDisable);
    }

    public void handleBuild(
        CreditsCodeArea codeArea, TableView<BuildSourceCodeError> errorTableView, VBox bottomPane,
        TabPane bottomTabPane, Tab bottomErrorTab) {
        deployTabController.smartBuildButton.setText(COMPILING);
        deployTabController.smartBuildButton.setDisable(true);
        errorTableView.setVisible(false);
        codeArea.setDisable(true);
        setFocusOnFeeField();
        supplyAsync(() -> compileSmartSourceCode(codeArea.getText()))
            .whenComplete(handleCallback(handleBuildResult(codeArea, errorTableView, bottomPane, bottomTabPane, bottomErrorTab)));
    }

    public void setFocusOnFeeField() {
        if (deployTabController.feeField.getText().isEmpty()) {
            deployTabController.feeField.requestFocus();
        }
    }

    private Callback<CompilationResult> handleBuildResult(
        CreditsCodeArea codeArea,
        TableView<BuildSourceCodeError> errorTableView, VBox bottomPane, TabPane bottomTabPane, Tab bottomErrorTab) {
        return new Callback<CompilationResult>() {
            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(CompilationResult compilationResult) {
                Platform.runLater(() -> {
                    codeArea.setDisable(false);
                    deployTabController.smartBuildButton.setText(BUILD);
                });
                if (checkNotError(bottomPane, errorTableView, compilationResult, bottomTabPane, bottomErrorTab)) {
                    compilationPackage = compilationResult.getCompilationPackage();
                    Platform.runLater(() -> {
                        deployTabController.smartBuildButton.setDisable(true);
                        deployTabController.smartDeployButton.setDisable(false);
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(() -> {
                    deployTabController.smartCodeArea.setDisable(false);
                    deployTabController.smartBuildButton.setDisable(false);
                    deployTabController.smartBuildButton.setText(BUILD);
                    FormUtils.showPlatformError(e.getMessage());
                });
                LOGGER.error("failed!", e);
            }
        };
    }

    boolean checkNotError(
        VBox smartErrorPanel, TableView<BuildSourceCodeError> tableView,
        CompilationResult compilationResult, TabPane bottomTabPane, Tab bottomErrorTab) {
        List<BuildSourceCodeError> errorsList = compilationResult.getErrors();
        if (errorsList.size() > 0) {
            Platform.runLater(() -> {
                deployTabController.smartBuildButton.setDisable(false);
                tableView.getItems().clear();
                tableView.getItems().addAll(errorsList);
                tableView.setVisible(true);
                tableView.setPrefHeight(smartErrorPanel.getPrefHeight());
                bottomTabPane.getSelectionModel().select(bottomErrorTab);
            });
            return false;
        }
        return true;
    }

    public void handleDeploy() {
        clearLabErr();
        String transactionFee = deployTabController.feeField.getText();
        if (toBigDecimal(transactionFee).compareTo(BigDecimal.ZERO) <= 0) {
            FormUtils.setErrorStyle(deployTabController.feeField);
            setFocusOnFeeField();
            return;
        }
        try {
            List<ByteBuffer> usedSmartsListFromField = getSmartsListFromField(deployTabController.usdSmarts.getText());
            String javaCode = SourceCodeUtils.normalizeSourceCode(deployTabController.smartCodeArea.getText());
            if (compilationPackage == null) {
                deployTabController.smartBuildButton.setDisable(false);
                deployTabController.smartDeployButton.setDisable(true);
                throw new CreditsException("Source code is not compiled");
            } else {
                List<ByteCodeObjectData> byteCodeObjectDataList = compilationPackageToByteCodeObjects(compilationPackage);

                Class<?> contractClass = compileSmartContractByteCode(byteCodeObjectDataList);
                TokenStandartData tokenStandartData = getTokenStandard(contractClass);

                SmartContractDeployData smartContractDeployData =
                    new SmartContractDeployData(javaCode, byteCodeObjectDataList, tokenStandartData);

                long idWithoutFirstTwoBits = getIdWithoutFirstTwoBits(AppState.getNodeApiService(), session.account, true);

                SmartContractData smartContractData = new SmartContractData(
                    generateSmartContractAddress(decodeFromBASE58(session.account), idWithoutFirstTwoBits,
                                                 byteCodeObjectDataList), decodeFromBASE58(session.account), smartContractDeployData, null, null);

                supplyAsync(() -> getCalcTransactionIdSourceTargetResult(AppState.getNodeApiService(), session.account,
                                                                         smartContractData.getBase58Address(), idWithoutFirstTwoBits), threadPool)
                    .thenApply(
                        (transactionData) -> createSmartContractTransaction(
                            transactionData,
                            FormUtils.getActualOfferedMaxFee16Bits(deployTabController.feeField),
                            smartContractData,
                            usedSmartsListFromField,
                            session))
                    .whenComplete(
                        handleCallback(handleDeployResult(getTokenInfo(contractClass, smartContractData))));
                loadVista(WALLET);
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
        ByteCodeContractClassLoader classLoader = new ByteCodeContractClassLoader();
        Class<?> contractClass = null;
        for (ByteCodeObjectData compilationUnit : smartContractByteCodeData) {
            Class<?> tempContractClass = classLoader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
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
                FormUtils.showPlatformInfo(String.format("%s%n%nSmart-contract address%n%n%s%n%ncopied to clipboard",
                                                         transactionFlowResultData.getMessage(), target));
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
        FormUtils.clearErrorStyle(deployTabController.feeField);
    }


    @Override
    public void formDeinitialize() {
        deployTabController.formDeinitialize();
    }

}
