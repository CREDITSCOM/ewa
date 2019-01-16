package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.VistaNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.Map;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class HeaderController implements FormInitializable{

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnWallet;

    @FXML
    private Button btnTransaction;

    @FXML
    private Button btnSmartExecute;

    @FXML
    private Button btnSmartDeploy;


    @FXML
    private void handleLogout() {
        VistaNavigator.loadVista(VistaNavigator.WELCOME,this);
    }

    public void handleWallet(ActionEvent actionEvent) {
        VistaNavigator.loadVista(VistaNavigator.WALLET,this);
    }

    public void handleTransaction(ActionEvent actionEvent) {
        VistaNavigator.loadVista(VistaNavigator.HISTORY,this);
    }

    public void handleSmartExecute(ActionEvent actionEvent) {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT,this);
    }

    public void handleSmartDeploy(ActionEvent actionEvent) {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT_DEPLOY,this);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {

    }
}
