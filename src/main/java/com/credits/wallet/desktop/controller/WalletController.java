package com.credits.wallet.desktop.controller;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.ApiTransactionThreadRunnable;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

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
    private TextField transText;

    @FXML
    private TableView<CoinTabRow> coins;

    @FXML
    private Pane coinsPane;

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
        AppState.text = transText.getText();

        // VALIDATE
        boolean isValidationSuccessful = true;
        clearLabErr();
        if (coins.getSelectionModel().getSelectedItem() == null ||
            coins.getSelectionModel().getSelectedItem().getName().isEmpty()) {
            labErrorCoin.setText(ERR_COIN);
            coinsPane.getStyleClass().add("credits-border-red");
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
        coins.setRowFactory( tv -> {
            TableRow<CoinTabRow> row = new TableRow<>();
            CoinTabRow rowData = row.getItem();
            ContextMenu cm = new ContextMenu();
                                /*
                                            MenuItem refreshItem = new MenuItem("Refresh");
                                            cm.getItems().add(refreshItem);
                                */
            MenuItem removeItem = new MenuItem("Delete");

            cm.getItems().add(removeItem);
            row.setOnMouseClicked(event -> {
                if ((! row.isEmpty()) && !row.getItem().getName().equals("") && !row.getItem().getName().equals("CS") ) {
                    row.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
                        if (t.getButton() == MouseButton.SECONDARY) {
                            removeItem.setOnAction(event1 -> {
                                coins.getItems().remove(row.getItem());
                                ConcurrentHashMap<String, String> coinsMap = CoinsUtils.getCoins();
                                coinsMap.remove(row.getItem().getName());
                                CoinsUtils.saveCoinsToFile(coinsMap);
                            });
                            cm.show(coins, t.getScreenX(), t.getScreenY());
                        }
                    });
                }
            });
            return row ;
        });

        CoinTabRow coinTabRow = new CoinTabRow();
        coinTabRow.setName("CS");
        DecimalFormat decimalFormat = new DecimalFormat("##0.00000000000000000000");
        coinTabRow.setBalance("Processing");
        synchronized (coins.getItems()) {
            coins.getItems().add(coinTabRow);
        }
        AppState.levelDbService.getAsyncBalance(AppState.account,
            new ApiTransactionThreadRunnable.Callback<BigDecimal>() {
                @Override
                public void onSuccess(BigDecimal resultData) {
                    coinTabRow.setBalance(
                        String.valueOf(decimalFormat.format(resultData.setScale(13, BigDecimal.ROUND_DOWN))));
                }

                @Override
                public void onError(Exception e) {
                    coinTabRow.setBalance("Receive error");
                    e.printStackTrace();
                }
            });



        try {
            CoinsUtils.getCoins().forEach((coin, smart) -> {
                CoinTabRow tempCoinTabRow = new CoinTabRow();
                tempCoinTabRow.setName(coin);
                tempCoinTabRow.setSmartName(smart);
                tempCoinTabRow.setBalance("Processing");
                try {
                    SmartContractUtils.getSmartContractBalance(CoinsUtils.getCoins().get(coin),
                        new ApiTransactionThreadRunnable.Callback<BigDecimal>() {
                            @Override
                            public void onSuccess(BigDecimal balance) throws LevelDbClientException {
                                synchronized (coins.getItems()) {
                                    tempCoinTabRow.setBalance(String.valueOf(decimalFormat.format(balance)));
                                    coins.getItems().add(tempCoinTabRow);
                                }
                            }
                            @Override
                            public void onError(Exception e) {
                                tempCoinTabRow.setBalance("Balance receive error");
                                e.printStackTrace();
                            }
                        });
                } catch (Exception e) {
                    tempCoinTabRow.setBalance("Receive error");
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearLabErr() {
        coinsPane.getStyleClass().remove("credits-border-red");
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
