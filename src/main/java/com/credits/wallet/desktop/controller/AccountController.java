package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Rustem.Saidaliyev on 26.11.2017.
 */
public class AccountController extends Controller implements Initializable {
    private static final String ERR_GETTING_BALANCE="Ошибка получения баланса";

    @FXML
    private Label wallet;

    @FXML
    private Label balance;

    @FXML
    private void handleDetails() {
        AppState.newAccount=false;
        App.showForm("/fxml/history.fxml", "Wallet");
    }

    public void initialize(URL location, ResourceBundle resources) {
        this.wallet.setText(AppState.account);

        String balanceInfo= Utils.callAPI("getbalance?account=" + AppState.account, ERR_GETTING_BALANCE);
        if (balanceInfo!=null) {
            JsonElement jelement = new JsonParser().parse(balanceInfo);
            JsonObject jObject=jelement.getAsJsonObject().get("response").getAsJsonObject().get("CS").getAsJsonObject();
            String balStr=Long.toString(jObject.get("integral").getAsLong())+
                    "."+Long.toString(jObject.get("fraction").getAsLong());
            this.balance.setText(balStr);
        }
    }
}
