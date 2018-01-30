package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import javafx.fxml.FXML;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller {
    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }
}
