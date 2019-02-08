package com.credits.wallet.desktop.utils;

import com.credits.client.node.pojo.TokenStandartData;
import com.credits.wallet.desktop.controller.DeployTabController;
import com.credits.wallet.desktop.controller.SmartContractDeployController;
import com.credits.wallet.desktop.controller.TreeViewController;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.collections.ObservableList;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeployControllerUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DeployControllerUtils.class);

    public static TokenStandartData getTokenStandard(Class<?> contractClass) {
        TokenStandartData tokenStandart = TokenStandartData.NotAToken;
        try {
            Class<?>[] interfaces = contractClass.getInterfaces();
            if (interfaces.length > 0) {
                Class<?> basicStandard = Class.forName("BasicStandard");
                Class<?> extendedStandard = Class.forName("ExtensionStandard");
                for (Class<?> _interface : interfaces) {
                    if (_interface.equals(basicStandard)) {
                        tokenStandart = TokenStandartData.CreditsBasic;
                    }
                    if (_interface.equals(extendedStandard)) {
                        tokenStandart = TokenStandartData.CreditsExtended;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.debug("can't find standard classes. Reason {}", e.getMessage());
        }
        return tokenStandart;
    }

    public static String getContractFromTemplate(String template) {
        try {
            return IOUtils.toString(
                DeployControllerUtils.class.getResourceAsStream("/template/" + template + ".template"), "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    public static String checkContractNameExist(String smartName, ObservableList<DeploySmartListItem> list) {
        AtomicBoolean flag = new AtomicBoolean(false);
        list.forEach(el->{
            if(el.name.equals(smartName)) {
                flag.set(true);
            }
        });
        if(flag.get()) {
            String nameWithBrace = smartName + "(";
            int maxNumber = 0;
            for (DeploySmartListItem existingItem : list){
                String existingName = existingItem.name;
                if(existingName.contains(nameWithBrace)){
                    int number = SmartContractsUtils.parseNumberOfDuplicateName(nameWithBrace.length(), existingName);
                    if (number != 0 && number > maxNumber) maxNumber = number;
                }
            }
            if(maxNumber>0) {
                return smartName + "(" + ++maxNumber + ")";
            }
            return smartName + "(1)";
        }
        return smartName;
    }

    public static void initSplitPane(SplitPane innerSplitPane, VBox errorPanel,
        TableView<BuildSourceCodeError> tableView) {
        for (SplitPane.Divider d : innerSplitPane.getDividers()) {
            d.positionProperty()
                .addListener((observable, oldValue, newValue) -> tableView.setPrefHeight(errorPanel.getHeight()));
        }
    }

    public static void initErrorTableView(VBox errorPanel, TableView<BuildSourceCodeError> errorTableView, CreditsCodeArea codeArea) {
        TableColumn<BuildSourceCodeError, String> tabErrorsColLine = new TableColumn<>();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setCellValueFactory(new PropertyValueFactory<>("line"));
        tabErrorsColLine.setPrefWidth(errorPanel.getPrefWidth() * 0.1);

        TableColumn<BuildSourceCodeError, String> tabErrorsColText = new TableColumn<>();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setCellValueFactory(new PropertyValueFactory<>("text"));
        tabErrorsColText.setPrefWidth(errorPanel.getPrefWidth() * 0.88);

        //errorTableView.setVisible(false);
        errorTableView.setPrefHeight(errorPanel.getPrefHeight());
        errorTableView.setPrefWidth(errorPanel.getPrefWidth());

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

    public static CreditsCodeArea initTabCodeArea(VBox sourceCodeBox, TreeViewController treeViewController,
        DeployTabController deployTabController, SmartContractDeployController smartContractDeployController) {
        CreditsCodeArea codeArea = CodeAreaUtils.initCodeArea(sourceCodeBox, false);
        treeViewController.parentController = deployTabController;
        treeViewController.refreshTreeView(codeArea);
        initTreeViewCodeAreaListener(codeArea, treeViewController, smartContractDeployController);
        return codeArea;
    }

    private static void initTreeViewCodeAreaListener(CreditsCodeArea codeArea, TreeViewController treeViewController,
        SmartContractDeployController smartContractDeployController) {
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (evt) -> {
            treeViewController.refreshTreeView(codeArea);
            smartContractDeployController.cleanCompilationPackage(false);
        });
    }





}
