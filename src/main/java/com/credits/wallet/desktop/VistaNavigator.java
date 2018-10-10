package com.credits.wallet.desktop;

import com.credits.wallet.desktop.controller.MainController;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * Utility class for controlling navigation between vistas.
 *
 * All methods on the navigator are static to facilitate
 * simple access from anywhere in the application.
 */
public class VistaNavigator {

    /**
     * Convenience constants for fxml layouts managed by the navigator.
     */
    public static final String ACCOUNT = "/fxml/account.fxml";
    public static final String FOOTER = "/fxml/footer.fxml";
    public static final String WELCOME = "/fxml/welcome.fxml";
    public static final String FORM_1 = "/fxml/form1.fxml";
    public static final String FORM_4 = "/fxml/form4.fxml";
    public static final String FORM_5 = "/fxml/form5.fxml";
    public static final String WALLET = "/fxml/wallet.fxml";
    public static final String FORM_7 = "/fxml/form7.fxml";
    public static final String FORM_8 = "/fxml/form8.fxml";
    public static final String HEADER = "/fxml/header.fxml";
    public static final String HISTORY = "/fxml/history.fxml";
    public static final String MAIN = "/fxml/main.fxml";
    public static final String NEW_COIN = "/fxml/new_coin.fxml";
    public static final String SMART_CONTRACT = "/fxml/smart_contract.fxml";
    public static final String SMART_CONTRACT_DEPLOY = "/fxml/smart_contract_deploy.fxml";
    public static final String TRANSACTION = "/fxml/transaction.fxml";
    /** The main application layout controller. */
    private static MainController mainController;

    /**
     * Stores the main controller for later use in navigation tasks.
     *
     * @param mainController the main application layout controller.
     */
    public static void setMainController(MainController mainController) {
        VistaNavigator.mainController = mainController;
    }

    /**
     * Loads the vista specified by the fxml file into the
     * vistaHolder pane of the main application layout.
     *
     * Previously loaded vista for the same fxml file are not cached.
     * The fxml is loaded anew and a new vista node hierarchy generated
     * every time this method is invoked.
     *
     * A more sophisticated load function could potentially add some
     * enhancements or optimizations, for example:
     *   cache FXMLLoaders
     *   cache loaded vista nodes, so they can be recalled or reused
     *   allow a user to specify vista node reuse or new creation
     *   allow back and forward history like a browser
     *
     * @param fxml the fxml file to be loaded.
     */
    public static void loadVista(String fxml) {
        try {
            mainController.setVista(
                FXMLLoader.load(
                    App.class.getResource(
                        fxml
                    )
                )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}