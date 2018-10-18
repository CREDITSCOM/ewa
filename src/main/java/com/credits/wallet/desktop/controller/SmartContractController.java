package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.common.utils.sourcecode.SourceCodeUtils;
import com.credits.leveldb.client.ApiTransactionThreadRunnable;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.thrift.generated.Variant;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.SmartContractTabRow;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static final String PERSONAL_CONTRACTS = "Personal contracts";
    private static final String FOUND_CONTRACTS = "Found contracts";
    private static final String SMART_CONTRACTS = "Smart contracts";
    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

    @FXML
    public ToggleButton tbFavourite;

    @FXML
    BorderPane bp;

    @FXML
    private Pane pControls;

    @FXML
    private TextField tfAddress;

    @FXML
    private ComboBox<MethodDeclaration> cbMethods;

    @FXML
    TableView<SmartContractTabRow> mySmart;

    @FXML
    TableView<SmartContractTabRow> favSmart;

    @FXML
    private TextField tfSearchAddress;

    @FXML
    private StackPane pCodePanel;

    @FXML
    private AnchorPane pParams;

    @FXML
    private AnchorPane pParamsContainer;

    private CodeArea codeArea;

    private SmartContractData currentSmartContract;

    private MethodDeclaration currentMethod;

    @FXML
    private void handleBack() {
        VistaNavigator.loadVista(VistaNavigator.WALLET);
    }

    @FXML
    private void handleCreate() {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT_DEPLOY);
    }

    @FXML
    private void handleSearch() {
        String address = tfSearchAddress.getText();
        try {
            SmartContractData smartContractData = AppState.levelDbService.getSmartContract(address);
            this.refreshFormState(smartContractData);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);
        this.tbFavourite.setVisible(false);
        this.pControls.setVisible(false);
        this.pCodePanel.setVisible(false);
        this.codeArea = SmartContractUtils.initCodeArea(this.pCodePanel, true);
        try {
            initSmartContracts();
        } catch (CreditsCommonException | LevelDbClientException | CreditsNodeException e) {
            e.printStackTrace();
        }
        this.codeArea.setEditable(false);
        this.codeArea.copy();
    }

    private void initSmartContracts() throws CreditsCommonException, LevelDbClientException, CreditsNodeException {

        setRowFactory(mySmart);
        setRowFactory(favSmart);
        FormUtils.addTooltipToColumnCells(mySmart.getColumns().get(0));
        FormUtils.addTooltipToColumnCells(favSmart.getColumns().get(0));

        mySmart.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        mySmart.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("fav"));
        favSmart.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        favSmart.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("fav"));
        initMySmartTab();
    }

    private void setRowFactory(TableView<SmartContractTabRow> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<SmartContractTabRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    this.refreshFormState(row.getItem().getSmartContractData());
                }
            });
            return row;
        });
    }

    private void setFavorite(ToggleButton favoriteButton, ConcurrentHashMap<String, SmartContractData> map,
        SmartContractData smartContractData) {
        String smartContractAddress = smartContractData.getStringAddress();
        if (map != null && map.size() > 0 && map.get(smartContractAddress) != null) {
            favoriteButton.setSelected(map.get(smartContractAddress).isFavorite());
        } else {
            favoriteButton.setSelected(false);
        }
    }

    private void setFavoriteButtonEvent(ToggleButton favoriteButton, SmartContractData smartContractData) {
        favoriteButton.setOnAction(event -> {
            smartContractData.setFavorite(favoriteButton.isSelected());
            saveFavorite(smartContractData);
            try {
                initMySmartTab();
            } catch (LevelDbClientException | CreditsNodeException | CreditsCommonException e) {
                e.printStackTrace();
            }
            initFavoriteTab();

        });
    }

    private void saveFavorite(SmartContractData smartContractData) {
        String contractName = smartContractData.getStringAddress();
        ConcurrentHashMap<String, SmartContractData> map = AppState.objectKeeper.deserialize();
        if (map != null && map.size() > 0) {
            map.put(contractName, smartContractData);
        } else {
            map = new ConcurrentHashMap<>();
            map.put(contractName, smartContractData);
        }
        AppState.objectKeeper.serialize(map);

    }

    private void refreshFormState(SmartContractData smartContractData) {
        if (smartContractData == null || smartContractData.getHashState().isEmpty() ||
            smartContractData.getAddress().length == 0) {
            this.tbFavourite.setVisible(false);
            this.pControls.setVisible(false);
            this.pCodePanel.setVisible(false);
        } else {
            this.pControls.setVisible(true);
            this.pCodePanel.setVisible(true);
            this.currentSmartContract = smartContractData;

            ConcurrentHashMap<String, SmartContractData> map = AppState.objectKeeper.deserialize();
            setFavorite(tbFavourite, map, smartContractData);
            setFavoriteButtonEvent(tbFavourite, currentSmartContract);
            this.tbFavourite.setVisible(true);
            String sourceCode = smartContractData.getSourceCode();
            this.tfAddress.setText(smartContractData.getStringAddress());
            List<MethodDeclaration> methods = SourceCodeUtils.parseMethods(sourceCode);
            cbMethods.getItems().clear();
            methods.forEach(method -> {
                method.setBody(null);
                cbMethods.getItems().add(method);
            });
            this.codeArea.clear();
            this.codeArea.replaceText(0, 0, SourceCodeUtils.formatSourceCode(sourceCode));
        }
    }

    @FXML
    private void cbMethodsOnAction() {
        this.pParams.setVisible(false);
        this.currentMethod = this.cbMethods.getSelectionModel().getSelectedItem();
        if (this.currentMethod == null) {
            return;
        }
        List<SingleVariableDeclaration> params = SourceCodeUtils.getMethodParameters(this.currentMethod);
        this.pParamsContainer.getChildren().clear();
        if (params.size() > 0) {
            this.pParams.setVisible(true);
            double layoutY = 10;
            for (SingleVariableDeclaration param : params) {
                TextField paramValueTextField = new TextField();
                paramValueTextField.setLayoutX(150);
                paramValueTextField.setLayoutY(layoutY);
                paramValueTextField.setStyle(
                    "-fx-background-color:  #fff; -fx-border-radius:15; -fx-border-width: 1; -fx-border-color:  #000; -fx-font-size: 16px");
                paramValueTextField.setPrefSize(300, 30);
                Label paramNameLabel = new Label(param.toString());
                paramNameLabel.setLayoutX(10);
                paramNameLabel.setLayoutY(layoutY + 5);
                paramNameLabel.setStyle("-fx-font-size: 18px");
                paramNameLabel.setLabelFor(paramValueTextField);
                this.pParamsContainer.getChildren().addAll(paramNameLabel, paramValueTextField);
                layoutY += 40;
            }
            this.pParamsContainer.setPrefHeight(layoutY);
        }
    }

    @FXML
    private void handleExecute() {
        try {
            String method = cbMethods.getSelectionModel().getSelectedItem().getName().getIdentifier();
            List<Variant> params = new ArrayList<>();
            List<SingleVariableDeclaration> currentMethodParams =
                SourceCodeUtils.getMethodParameters(this.currentMethod);

            ObservableList<Node> paramsContainerChildren = this.pParamsContainer.getChildren();

            int i = 0;
            for (Node node : paramsContainerChildren) {
                if (node instanceof TextField) {
                    String paramValue = ((TextField) node).getText();
                    SingleVariableDeclaration variableDeclaration = currentMethodParams.get(i);
                    String className = SourceCodeUtils.parseClassName(variableDeclaration);
                    params.addAll(SourceCodeUtils.createVariantObject(className, paramValue));
                    ++i;
                }

            }

            SmartContractData smartContractData = this.currentSmartContract;
            ApiUtils.executeSmartContractProcess(method, params, smartContractData,
                new ApiTransactionThreadRunnable.Callback() {
                    @Override
                    public void onSuccess(ApiResponseData resultData) {
                        Variant res = resultData.getScExecRetVal();
                        if (res != null) {
                            String retVal = res.toString() + '\n';
                            LOGGER.info("Return value is {}", retVal);
                            FormUtils.showPlatformInfo(
                                "Execute smart contract was success: return value is: " + retVal);
                        } else {
                            FormUtils.showPlatformInfo("Execute smart contract was success");
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        FormUtils.showPlatformError(e.getMessage());
                    }
                });
        } catch (CreditsException e) {
            LOGGER.error(e.toString(), e);
            FormUtils.showError(e.toString());
        }
    }

    private void initMySmartTab() throws LevelDbClientException, CreditsNodeException, CreditsCommonException {
        mySmart.getItems().clear();
        List<SmartContractData> smartContracts = AppState.levelDbService.getSmartContracts(AppState.account);
        ConcurrentHashMap<String, SmartContractData> map = AppState.objectKeeper.deserialize();
        smartContracts.forEach(smartContractData -> {
            ToggleButton favoriteButton = new ToggleButton();
            setFavorite(favoriteButton, map, smartContractData);

            setFavoriteButtonEvent(favoriteButton, smartContractData);
            mySmart.getItems()
                .add(new SmartContractTabRow(smartContractData.getStringAddress(), favoriteButton,
                    smartContractData));
        });
        if (this.currentSmartContract != null) {
            setFavorite(tbFavourite, map, this.currentSmartContract);
        }

    }


    @FXML
    private void initFavoriteTab() {
        favSmart.getItems().clear();
        try {
            Map<String, SmartContractData> map = AppState.objectKeeper.deserialize();
            if (map != null && map.size() > 0) {
                map.forEach((smartName, smartContractData) -> {
                    if (smartContractData.isFavorite()) {
                        ToggleButton favoriteButton = new ToggleButton();
                        favoriteButton.setSelected(true);
                        setFavoriteButtonEvent(favoriteButton, smartContractData);
                        favSmart.getItems()
                            .add(new SmartContractTabRow(smartContractData.getStringAddress(),
                                favoriteButton, smartContractData));
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            FormUtils.showError(e.getMessage());
        }
    }

}