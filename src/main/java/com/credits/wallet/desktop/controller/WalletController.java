package com.credits.wallet.desktop.controller;

import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.client.node.thrift.call.ThriftCallThread.Callback;
import com.credits.client.node.util.Validator;
import com.credits.general.util.Converter;
import com.credits.general.util.exception.ConverterException;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.CoinTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.NumberUtils;
import javafx.event.EventHandler;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static com.credits.wallet.desktop.AppState.CREDITS_DECIMAL;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.amount;
import static com.credits.wallet.desktop.AppState.coin;
import static com.credits.wallet.desktop.AppState.contractInteractionService;
import static com.credits.wallet.desktop.AppState.newAccount;
import static com.credits.wallet.desktop.AppState.noClearForm6;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.AppState.text;
import static com.credits.wallet.desktop.AppState.toAddress;
import static com.credits.wallet.desktop.AppState.transactionFeePercent;
import static com.credits.wallet.desktop.AppState.transactionFeeValue;
import static com.credits.wallet.desktop.utils.CoinsUtils.getCoins;
import static com.credits.wallet.desktop.utils.CoinsUtils.saveCoinsToFile;
import static org.apache.commons.lang3.StringUtils.repeat;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class WalletController extends Controller implements Initializable {

    private static final String ERR_COIN = "Coin must be selected";
    private static final String ERR_AMOUNT = "Amount must be greater than 0";
    private static final String ERR_TO_ADDRESS = "To address must not be empty";
    private static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private final String WAITING_STATE_MESSAGE = "Processing...";
    private final String CREDITS_TOKEN_NAME = "CS";
    @FXML
    BorderPane bp;
    @FXML
    private Label wallet;
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

    @FXML
    private void handleLogout() {
        VistaNavigator.loadVista("/fxml/welcome.fxml");
    }

    @FXML
    private void handleDetails() {
        newAccount = false;
        VistaNavigator.loadVista("/fxml/history.fxml");
    }

    @FXML
    private void handleAddCoin() {
        VistaNavigator.loadVista(VistaNavigator.NEW_COIN);
    }

    @FXML
    private void handleSmartContract() {
        newAccount = false;
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

    private void initializeCoinsView() {
        coins.getItems().clear();
        coins.setRowFactory(tv -> {
            TableRow<CoinTabRow> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
/*
            MenuItem refreshItem = new MenuItem("Refresh");
            contextMenu.getItems().add(refreshItem);
*/
            MenuItem removeItem = new MenuItem("Delete");

            contextMenu.getItems().add(removeItem);
            row.setOnMouseClicked(handleDeleteToken(row, contextMenu, removeItem));
            return row;
        });

        List<CoinTabRow> coinRows = coins.getItems();
        addCsCoinRow(coinRows);
        getCoins().forEach((coinName, smartContract) -> addUserCoin(coinName, smartContract, coinRows));
    }

    private EventHandler<MouseEvent> handleDeleteToken(TableRow<CoinTabRow> row, ContextMenu cm, MenuItem removeItem) {
        return event -> {
            if ((!row.isEmpty()) && !row.getItem().getName().equals("") && !row.getItem().getName().equals("CS")) {
                row.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
                    if (t.getButton() == MouseButton.SECONDARY) {
                        removeItem.setOnAction(event1 -> {
                            coins.getItems().remove(row.getItem());
                            ConcurrentHashMap<String, String> coinsMap = getCoins();
                            coinsMap.remove(row.getItem().getName());
                            saveCoinsToFile(coinsMap);
                        });
                        cm.show(coins, t.getScreenX(), t.getScreenY());
                    }
                });
            }
        };
    }

    private void addCsCoinRow(List<CoinTabRow> coinRows) {
        CoinTabRow newCoinRow = new CoinTabRow(CREDITS_TOKEN_NAME, WAITING_STATE_MESSAGE, null);
        DecimalFormat decimalFormat = new DecimalFormat("##0." + repeat('0', CREDITS_DECIMAL));
        nodeApiService.getAsyncBalance(account, updateCoinValue(coinRows, newCoinRow, decimalFormat));
        coinRows.add(newCoinRow);
    }

    private void addUserCoin(String coinName, String smartContractAddress, List<CoinTabRow> coinRows) {
        CoinTabRow newCoinRow = new CoinTabRow(coinName, WAITING_STATE_MESSAGE, smartContractAddress);
        DecimalFormat decimalFormat = new DecimalFormat("##0.000000000000000000"); // fixme must use the method "tokenContract.decimal()"
        contractInteractionService.getSmartContractBalance(smartContractAddress, updateCoinValue(coinRows, newCoinRow, decimalFormat));
        coinRows.add(newCoinRow);
    }

    private Callback<BigDecimal> updateCoinValue(List<CoinTabRow> coinsList, CoinTabRow coinRow, DecimalFormat decimalFormat) {
        return new Callback<BigDecimal>() {
            @Override
            public void onSuccess(BigDecimal balance) {
                coinRow.setBalance(decimalFormat.format(balance));
                synchronized (coinsList) {
                    coinsList.add(coinRow);
                }
            }

            @Override
            public void onError(Throwable e) {
                coinRow.setBalance("Receive error");
                e.printStackTrace();
            }
        };
    }

    @FXML
    private void handleGenerate() {
        amount = Converter.toBigDecimal(numAmount.getText());
        toAddress = txKey.getText();
        text = transText.getText();

        // VALIDATE
        boolean isValidationSuccessful = true;
        clearLabErr();
        if (coins.getSelectionModel().getSelectedItem() == null || coins.getSelectionModel().getSelectedItem().getName().isEmpty()) {
            labErrorCoin.setText(ERR_COIN);
            coinsPane.getStyleClass().add("credits-border-red");
            isValidationSuccessful = false;
        } else {
            coin = coins.getSelectionModel().getSelectedItem().getName();
        }
        if (toAddress == null || toAddress.isEmpty()) {
            labErrorKey.setText(ERR_TO_ADDRESS);
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
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
            Validator.validateToAddress(toAddress);
        } catch (ConverterException e) {
            labErrorKey.setText("Invalid Address");
            txKey.setStyle(txKey.getStyle().replace("-fx-border-color: #ececec", "-fx-border-color: red"));
            isValidationSuccessful = false;
        }

        if (isValidationSuccessful) {
            VistaNavigator.loadVista(VistaNavigator.FORM_7);
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.addTooltipToColumnCells(coins.getColumns().get(0));
        FormUtils.addTooltipToColumnCells(coins.getColumns().get(1));
        coins.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        coins.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("balance"));

        initializeCoinsView();

        wallet.setText(account);

        FormUtils.resizeForm(bp);
        clearLabErr();
        // Fill coin list
        NodeApiServiceImpl.account = account;

        numAmount.textProperty()
                 .addListener((observable, oldValue, newValue) -> refreshTransactionFeePercent(Converter.toBigDecimal(numFee.getText()),
                     Converter.toBigDecimal(newValue)));

        numFee.textProperty()
              .addListener((observable, oldValue, newValue) -> refreshTransactionFeePercent(Converter.toBigDecimal(newValue),
                  Converter.toBigDecimal(numAmount.getText())));

        numAmount.setOnKeyReleased(event -> NumberUtils.correctNum(event.getText(), numAmount));

        numFee.setOnKeyReleased(event -> NumberUtils.correctNum(event.getText(), numFee));

        if (noClearForm6) {
            txKey.setText(toAddress);
            numAmount.setText(Converter.toString(amount));
            numFee.setText(Converter.toString(transactionFeeValue));

            noClearForm6 = false;
        }
    }

    private void refreshTransactionFeePercent(BigDecimal transactionFeeValue, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            transactionFeePercent = BigDecimal.ZERO;
        } else {
            transactionFeePercent = (transactionFeeValue.multiply(new BigDecimal("100"))).divide(amount, 18, RoundingMode.HALF_UP);
        }
    }
}
