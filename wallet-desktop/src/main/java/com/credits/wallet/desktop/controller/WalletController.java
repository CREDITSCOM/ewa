package com.credits.wallet.desktop.controller;

import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.client.node.util.Validator;
import com.credits.general.util.Callback;
import com.credits.general.util.Converter;
import com.credits.general.util.exception.ConverterException;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.CoinTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.NumberUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.wallet.desktop.AppState.CREDITS_DECIMAL;
import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.AppState.amount;
import static com.credits.wallet.desktop.AppState.coin;
import static com.credits.wallet.desktop.AppState.coinsKeeper;
import static com.credits.wallet.desktop.AppState.contractInteractionService;
import static com.credits.wallet.desktop.AppState.noClearForm6;
import static com.credits.wallet.desktop.AppState.nodeApiService;
import static com.credits.wallet.desktop.AppState.text;
import static com.credits.wallet.desktop.AppState.toAddress;
import static com.credits.wallet.desktop.AppState.transactionFeePercent;
import static com.credits.wallet.desktop.AppState.transactionFeeValue;
import static org.apache.commons.lang3.StringUtils.repeat;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class WalletController implements Initializable {

    private static final String ERR_COIN = "Coin must be selected";
    private static final String ERR_AMOUNT = "Amount must be greater than 0";
    private static final String ERR_TO_ADDRESS = "To address must not be empty";
    private static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private final String WAITING_STATE_MESSAGE = "processing...";
    private final String ERROR_STATE_MESSAGE = "not available";
    private final DecimalFormat creditsDecimalFormat = new DecimalFormat("##0." + repeat('0', CREDITS_DECIMAL));
    private Lock lock = new ReentrantLock(true);
    public final String CREDITS_TOKEN_NAME = "CS";

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
    private volatile TableView<CoinTabRow> coinsTableView;
    private String GET_ERROR_MESSAGE;

    ContextMenu contextMenu = new ContextMenu();

    @FXML
    private void handleLogout() {
        VistaNavigator.loadVista("/fxml/welcome.fxml");
    }

    @FXML
    private void handleAddCoin() {
        VistaNavigator.loadVista(VistaNavigator.NEW_COIN);
    }

    @FXML
    private void handleCopy() {
        StringSelection selection = new StringSelection(wallet.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @FXML
    private void handleRefreshBalance() {
        updateCoins(coinsTableView);
    }

    private void updateCoins(TableView<CoinTabRow> tableView) {
        ObservableList<CoinTabRow> tableViewItems = tableView.getItems();
        addOrUpdateCsCoinRow(tableViewItems);
        coinsKeeper.getKeptObject(ConcurrentHashMap::new).forEach((coinName, contractAddress) -> addOrUpdateUserCoinRow(tableViewItems, coinName, contractAddress));
    }

    private EventHandler<MouseEvent> handleDeleteToken(TableRow<CoinTabRow> row) {
        return event -> {
            if ((!row.isEmpty()) && !row.getItem().getName().equals("") && !row.getItem().getName().equals(CREDITS_TOKEN_NAME)) {
                row.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
                    if (t.getButton() == MouseButton.SECONDARY) {
                        Platform.runLater(() -> {
                            contextMenu.getItems().clear();
                            contextMenu.hide();
                            MenuItem removeItem = new MenuItem("Delete");
                            contextMenu.getItems().add(removeItem);

                            removeItem.setOnAction(event1 -> {
                                coinsTableView.getItems().remove(row.getItem());
                                coinsKeeper.modify(coinsKeeper.new Modifier() {
                                    @Override
                                    public ConcurrentHashMap<String, String> modify(ConcurrentHashMap<String, String> restoredObject) {
                                        restoredObject.remove(row.getItem().getName());
                                        return restoredObject;
                                    }
                                });
                            });
                            contextMenu.show(coinsTableView, t.getScreenX(), t.getScreenY());
                        });
                    }
                });
            }
        };
    }

    private void addOrUpdateCsCoinRow(ObservableList<CoinTabRow> tableViewItems) {
        CoinTabRow coinRow = getCoinTabRow(tableViewItems, CREDITS_TOKEN_NAME, null);
        changeTableViewValue(coinRow, WAITING_STATE_MESSAGE);
        async(() -> nodeApiService.getBalance(account), handleUpdateCoinValue(coinRow, creditsDecimalFormat));
    }

    private void addOrUpdateUserCoinRow(ObservableList<CoinTabRow> tableViewItems, String coinName, String smartContractAddress) {
        CoinTabRow coinRow = getCoinTabRow(tableViewItems, coinName, smartContractAddress);
        if(coinRow.getLock().tryLock()) {
            LOGGER.debug("{} take lock {}", coinName, coinRow.getLock().hashCode());
            changeTableViewValue(coinRow, WAITING_STATE_MESSAGE);
            DecimalFormat decimalFormat = new DecimalFormat("##0.000000000000000000"); // fixme must use the method "tokenContract.decimal()"
            contractInteractionService.getSmartContractBalance(smartContractAddress, handleUpdateCoinValue(coinRow, decimalFormat));
        }
    }

    private void changeTableViewValue(CoinTabRow coinRow, String value) {
        coinsTableView.refresh();
        coinRow.setValue(value);
    }

    private CoinTabRow getCoinTabRow(ObservableList<CoinTabRow> tableViewItems, String tokenName, String contractAddress) {
        CoinTabRow coinRow = new CoinTabRow(tokenName, WAITING_STATE_MESSAGE, contractAddress);
        return tableViewItems.stream()
            .filter(foundCoinRow -> foundCoinRow.getName().equals(coinRow.getName()))
            .findFirst()
            .orElseGet(() -> {
                tableViewItems.add(coinRow);
                return coinRow;
            });
    }

    private Callback<BigDecimal> handleUpdateCoinValue(CoinTabRow coinRow, DecimalFormat decimalFormat) {
        return new Callback<BigDecimal>() {
            @Override
            public void onSuccess(BigDecimal balance) {
                Platform.runLater(() -> {
                    changeTableViewValue(coinRow, decimalFormat.format(balance));
                    coinRow.getLock().unlock();
                });
            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(() -> {
                    changeTableViewValue(coinRow, ERROR_STATE_MESSAGE);
                    coinRow.getLock().unlock();
                });
                LOGGER.error("cant't update balance token {}. Reason: {}", coinRow.getName(), e.getMessage());
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
        if (coinsTableView.getSelectionModel().getSelectedItem() == null || coinsTableView.getSelectionModel().getSelectedItem().getName().isEmpty()) {
            labErrorCoin.setText(ERR_COIN);
            coinsTableView.getStyleClass().add("credits-border-red");
            isValidationSuccessful = false;
        } else {
            coin = coinsTableView.getSelectionModel().getSelectedItem().getName();
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
        coinsTableView.getStyleClass().remove("credits-border-red");
        labErrorCoin.setText("");
        labErrorAmount.setText("");
        labErrorFee.setText("");
        labErrorKey.setText("");

        /*cbCoin.setStyle(cbCoin.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));*/
        txKey.setStyle(txKey.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        numAmount.setStyle(numAmount.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
        numFee.setStyle(numFee.getStyle().replace("-fx-border-color: red", "-fx-border-color: #ececec"));
    }

    private void initializeTable(TableView<CoinTabRow> tableView) {
        setRowFactory(tableView);
        initColumns(tableView);
    }

    private void initColumns(TableView<CoinTabRow> tableView) {
        TableColumn<CoinTabRow, String> nameColumn = new TableColumn<>();
        nameColumn.setPrefWidth(tableView.getPrefWidth() * 0.55);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        FormUtils.addTooltipToColumnCells(nameColumn);

        TableColumn<CoinTabRow, String> balanceColumn = new TableColumn<>();
        balanceColumn.setPrefWidth(tableView.getPrefWidth() * 0.4);
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceColumn.setStyle("-fx-alignment: top_right");
        FormUtils.addTooltipToColumnCells(balanceColumn);

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(balanceColumn);
    }

    private void setRowFactory(TableView<CoinTabRow> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<CoinTabRow> row = new TableRow<>();
            row.setOnMouseClicked(handleDeleteToken(row));
            return row;
        });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FormUtils.resizeForm(bp);

        initializeTable(coinsTableView);
        updateCoins(coinsTableView);

        NodeApiServiceImpl.account = account;
        wallet.setText(account);

        clearLabErr();

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
