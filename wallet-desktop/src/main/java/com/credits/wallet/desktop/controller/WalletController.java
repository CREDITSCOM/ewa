package com.credits.wallet.desktop.controller;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.util.Validator;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.VistaNavigator;
import com.credits.wallet.desktop.struct.CoinTabRow;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.NumberUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.credits.client.node.service.NodeApiServiceImpl.async;
import static com.credits.wallet.desktop.AppState.CREDITS_DECIMAL;
import static com.credits.wallet.desktop.AppState.CREDITS_TOKEN_NAME;
import static org.apache.commons.lang3.StringUtils.repeat;


public class WalletController extends AbstractController {

    private static final String ERR_FEE = "Fee must be greater than 0";
    private static final String ERR_COIN = "Coin must be selected";
    private static final String ERR_AMOUNT = "Amount must be greater than 0";
    private static final String ERR_TO_ADDRESS = "To address must not be empty";
    private static Logger LOGGER = LoggerFactory.getLogger(WalletController.class);
    private final String WAITING_STATE_MESSAGE = "processing...";
    private final String ERROR_STATE_MESSAGE = "not available";
    private final DecimalFormat creditsDecimalFormat = new DecimalFormat("##0." + repeat('0', CREDITS_DECIMAL));
    ContextMenu contextMenu = new ContextMenu();

    @FXML
    private Label publicWalletID;
    @FXML
    private Label coinsErrorLabel;
    @FXML
    private Label addressErrorLabel;
    @FXML
    private Label amountErrorLabel;
    @FXML
    private Label feeErrorLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField feeField;
    @FXML
    private TextField transText;
    @FXML
    private TableView<CoinTabRow> coinsTableView;
    @FXML
    private Label actualOfferedMaxFeeLabel;

    @FXML
    private void handleLogout() {
        closeSession();
        VistaNavigator.loadVista(VistaNavigator.WELCOME);
    }

    @FXML
    private void handleAddCoin() {
        VistaNavigator.loadVista(VistaNavigator.NEW_COIN);
    }

    @FXML
    private void handleCopy() {
        StringSelection selection = new StringSelection(publicWalletID.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @FXML
    private void handleRefreshBalance() {
        updateCoins(coinsTableView);
    }

    @FXML
    private void handleGenerate() {
        String transactionAmount = amountField.getText();
        String transactionFee = feeField.getText();
        String transactionToAddress = addressField.getText();
        String transactionText = transText.getText();

        // VALIDATE
        AtomicBoolean isValidationSuccessful = new AtomicBoolean(true);
        clearLabErr();
        if (coinsTableView.getSelectionModel().getSelectedItem() == null ||
            coinsTableView.getSelectionModel().getSelectedItem().getName().isEmpty()) {
            FormUtils.validateTable(coinsTableView, coinsErrorLabel, ERR_COIN, isValidationSuccessful);
        }
        if (transactionToAddress == null || transactionToAddress.isEmpty()) {
            FormUtils.validateField(addressField, addressErrorLabel, ERR_TO_ADDRESS, isValidationSuccessful);
        }
        if (GeneralConverter.toBigDecimal(transactionAmount).compareTo(BigDecimal.ZERO) <= 0) {
            FormUtils.validateField(amountField, amountErrorLabel, ERR_AMOUNT, isValidationSuccessful);
        }
        if (GeneralConverter.toBigDecimal(transactionFee).compareTo(BigDecimal.ZERO) <= 0) {
            FormUtils.validateField(feeField, feeErrorLabel, ERR_FEE, isValidationSuccessful);
        }
        try {
            Validator.validateToAddress(transactionToAddress);
        } catch (NodeClientException e) {
            FormUtils.validateField(addressField, addressErrorLabel, "Invalid Address", isValidationSuccessful);
        }

        if (isValidationSuccessful.get()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("coinType", coinsTableView.getSelectionModel().getSelectedItem().getName());
            params.put("transactionToAddress", transactionToAddress);
            params.put("transactionAmount", transactionAmount);
            params.put("transactionFee", transactionFee);
            params.put("transactionText", transactionText);
            params.put("actualOfferedMaxFee16Bits", FormUtils.getActualOfferedMaxFee16Bits(feeField));

            VistaNavigator.loadVista(VistaNavigator.FORM_7, params);
        }
    }

    private void updateCoins(TableView<CoinTabRow> tableView) {
        ObservableList<CoinTabRow> tableViewItems = tableView.getItems();
        addOrUpdateCsCoinRow(tableViewItems);
        session.coinsKeeper.getKeptObject()
            .orElseGet(ConcurrentHashMap::new)
            .forEach((coinName, contractAddress) -> addOrUpdateUserCoinRow(tableViewItems, coinName, contractAddress));
    }

    private EventHandler<MouseEvent> handleDeleteToken(TableRow<CoinTabRow> row) {
        return event -> {
            if ((!row.isEmpty()) && !row.getItem().getName().equals("") &&
                !row.getItem().getName().equals(CREDITS_TOKEN_NAME)) {
                row.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
                    if (t.getButton() == MouseButton.SECONDARY) {
                        Platform.runLater(() -> {
                            contextMenu.getItems().clear();
                            contextMenu.hide();
                            MenuItem removeItem = new MenuItem("Delete");
                            contextMenu.getItems().add(removeItem);

                            removeItem.setOnAction(event1 -> {
                                coinsTableView.getItems().remove(row.getItem());
                                session.coinsKeeper.modify(session.coinsKeeper.new Modifier() {
                                    @Override
                                    public ConcurrentHashMap<String, String> modify(
                                        ConcurrentHashMap<String, String> restoredObject) {
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
        async(() -> AppState.getNodeApiService().getBalance(session.account), handleUpdateCoinValue(coinRow, creditsDecimalFormat));
    }

    private void addOrUpdateUserCoinRow(ObservableList<CoinTabRow> tableViewItems, String coinName,
        String smartContractAddress) {
        CoinTabRow coinRow = getCoinTabRow(tableViewItems, coinName, smartContractAddress);
        if (coinRow.getLock().tryLock()) {
            changeTableViewValue(coinRow, WAITING_STATE_MESSAGE);
            DecimalFormat decimalFormat =
                new DecimalFormat("##0.000000000000000000"); // todo must use the method "tokenContract.decimal()"
            session.contractInteractionService.getSmartContractBalance(smartContractAddress,
                handleUpdateCoinValue(coinRow, decimalFormat));
        }
    }

    private void changeTableViewValue(CoinTabRow coinRow, String value) {
        coinsTableView.refresh();
        coinRow.setValue(value);
    }

    private CoinTabRow getCoinTabRow(ObservableList<CoinTabRow> tableViewItems, String tokenName,
        String contractAddress) {
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
                Platform.runLater(() -> changeTableViewValue(coinRow, decimalFormat.format(balance)));
                coinRow.getLock().unlock();
            }

            @Override
            public void onError(Throwable e) {
                Platform.runLater(() -> changeTableViewValue(coinRow, ERROR_STATE_MESSAGE));
                LOGGER.error("cant't update balance token {}. Reason: {}", coinRow.getName(), e.getMessage());
                coinRow.getLock().unlock();
            }
        };
    }

    private void clearLabErr() {
        FormUtils.clearErrorOnTable(coinsTableView, coinsErrorLabel);
        FormUtils.clearErrorOnField(addressField, addressErrorLabel);
        FormUtils.clearErrorOnField(amountField, amountErrorLabel);
        FormUtils.clearErrorOnField(feeField, feeErrorLabel);
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
    public void initializeForm(Map<String, Object> objects) {
        clearLabErr();

        initializeTable(coinsTableView);
        updateCoins(coinsTableView);

        publicWalletID.setText(session.account);

        FormUtils.initFeeField(feeField,actualOfferedMaxFeeLabel);

        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            newValue = NumberUtils.getCorrectNum(newValue);
            setFieldValue(amountField, newValue);
        });

        if (objects != null) {
            addressField.setText(objects.get("transactionToAddress").toString());
            amountField.setText(objects.get("transactionAmount").toString());
            feeField.setText(objects.get("transactionFee").toString());
            transText.setText(objects.get("transactionText").toString());
            int i = 0;
            for (CoinTabRow item : coinsTableView.getItems()) {
                if (item.getName().equals(objects.get("coinType").toString())) {
                    coinsTableView.getSelectionModel().select(i);
                    break;
                }
                i++;
            }
        }
    }

    private void setFieldValue(TextField tf, String newValue) {
        if(newValue.isEmpty()) {
            tf.setText("");
        } else {
            tf.setText(newValue);
        }
    }


    @Override
    public void formDeinitialize() {
    }
}
