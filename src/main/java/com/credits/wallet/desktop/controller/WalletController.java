package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.service.LevelDbServiceImpl;
import com.credits.leveldb.client.util.Validator;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.CoinTabRow;
import com.credits.wallet.desktop.utils.CoinsUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.NumberUtils;
import com.credits.wallet.desktop.utils.SmartContractUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class WalletController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);

    private static final String ERR_COIN = "Coin must be selected";
    private static final String ERR_AMOUNT = "Amount must be greater than 0";
    private static final String ERR_TO_ADDRESS = "To address must not be empty";

    @FXML
    private Label wallet;

    @FXML
    BorderPane bp;

    @FXML
    private Label labErrorCoin;
    @FXML
    private Label labErrorKey;
    @FXML
    private Label labErrorAmount;
    @FXML
    private Label labErrorFee;

    @FXML
    private TextField txKey;

    @FXML
    private TextField numAmount;

    @FXML
    private TextField numFee;

    @FXML
    private TableView<CoinTabRow> coins;

    private void refreshTransactionFeePercent(BigDecimal transactionFeeValue, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            AppState.transactionFeePercent = BigDecimal.ZERO;
        } else {
            AppState.transactionFeePercent =
                (transactionFeeValue.multiply(new BigDecimal("100"))).divide(amount, 18, RoundingMode.HALF_UP);
        }
    }


    @FXML
    private void handleLogout() {
        VistaNavigator.loadVista("/fxml/welcome.fxml");
    }

    @FXML
    private void handleDetails() {
        AppState.newAccount = false;
        VistaNavigator.loadVista("/fxml/history.fxml");
    }

    @FXML
    private void handleAddCoin() {
        VistaNavigator.loadVista(VistaNavigator.NEW_COIN);
    }

    @FXML
    private void handleSmartContract() {
        AppState.newAccount = false;
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT);
    }


    @FXML
    private void handleCopy() {
        StringSelection selection = new StringSelection(wallet.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @FXML
    private void handleRefreshBalance() {
        initializeCoinsView();
    }

    @FXML
    private void handleGenerate() throws CreditsException {
        AppState.amount = Converter.toBigDecimal(numAmount.getText());
        AppState.toAddress = txKey.getText();

        // VALIDATE
        boolean isValidationSuccessful = true;
        clearLabErr();
        if (coins.getSelectionModel().getSelectedItem()==null ||
            coins.getSelectionModel().getSelectedItem().getName().isEmpty()) {
            labErrorCoin.setText(ERR_COIN);
            /*cbCoin.setStyle(cbCoin.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));*/
            isValidationSuccessful = false;
        } else {
            AppState.coin = coins.getSelectionModel().getSelectedItem().getName();
        }
        if (AppState.toAddress == null || AppState.toAddress.isEmpty()) {
            labErrorKey.setText(ERR_TO_ADDRESS);
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }
        if (AppState.amount.compareTo(BigDecimal.ZERO) <= 0) {
            labErrorAmount.setText(ERR_AMOUNT);
            numAmount.setStyle(numAmount.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }
        /*
        if (AppState.transactionFeeValue.compareTo(BigDecimal.ZERO) <= 0) {
            labErrorFee.setText(ERR_FEE);
            numFee.setStyle(numFee.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }
        */
        try {
            Validator.validateToAddress(AppState.toAddress);
        } catch (LevelDbClientException e) {
            labErrorKey.setText("Invalid Address");
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (isValidationSuccessful) {
            VistaNavigator.loadVista(VistaNavigator.FORM_7);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.addTooltipToColumnCells(coins.getColumns().get(0));
        FormUtils.addTooltipToColumnCells(coins.getColumns().get(1));
        coins.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        coins.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("balance"));

        initializeCoinsView();

        this.wallet.setText(AppState.account);

        FormUtils.resizeForm(bp);
        clearLabErr();
        // Fill coin list
        LevelDbServiceImpl.account = AppState.account;

        this.numAmount.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                refreshTransactionFeePercent(Converter.toBigDecimal(this.numFee.getText()),
                    Converter.toBigDecimal(newValue));
            } catch (CreditsException e) {
                LOGGER.error(e.getMessage());
            }
        });

        this.numFee.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                refreshTransactionFeePercent(Converter.toBigDecimal(newValue),
                    Converter.toBigDecimal(this.numAmount.getText()));
            } catch (CreditsException e) {
                LOGGER.error(e.getMessage());
            }
        });

        this.numAmount.setOnKeyReleased(event -> {
            NumberUtils.correctNum(event.getText(), this.numAmount);
        });

        this.numFee.setOnKeyReleased(event -> {
            NumberUtils.correctNum(event.getText(), this.numFee);
        });

        if (AppState.noClearForm6) {
            txKey.setText(AppState.toAddress);
            numAmount.setText(Converter.toString(AppState.amount));
            numFee.setText(Converter.toString(AppState.transactionFeeValue));

            AppState.noClearForm6 = false;
        }
    }

    private void initializeCoinsView() {
        coins.getItems().clear();
        CoinTabRow coinTabRow = new CoinTabRow();
        coinTabRow.setName("CS");

        try {
            coinTabRow.setBalance(String.valueOf(
                AppState.levelDbService.getBalance(AppState.account).setScale(13, BigDecimal.ROUND_DOWN)));
        } catch (LevelDbClientException | CreditsNodeException | CreditsCommonException e) {
            coinTabRow.setBalance("Receive error");
            e.printStackTrace();
        }
        coins.getItems().add(coinTabRow);

        CoinsUtils.getCoins().forEach((coin, smart) -> {
            CoinTabRow tempCoinTabRow = new CoinTabRow();
            tempCoinTabRow.setName(coin);
            tempCoinTabRow.setSmartName(smart);
            try {
                tempCoinTabRow.setBalance(
                String.valueOf(SmartContractUtils.getSmartContractBalance(CoinsUtils.getCoins().get(coin))));
            } catch (CreditsNodeException | CreditsCommonException | LevelDbClientException e) {
                tempCoinTabRow.setBalance("Receive error");
                e.printStackTrace();
            }
            coins.getItems().add(tempCoinTabRow);
        });
    }

    private void clearLabErr() {
        labErrorCoin.setText("");
        labErrorAmount.setText("");
        labErrorFee.setText("");
        labErrorKey.setText("");

        /*cbCoin.setStyle(cbCoin.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));*/
        txKey.setStyle(txKey.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        numAmount.setStyle(numAmount.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        numFee.setStyle(numFee.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }
}
