package com.credits.wallet.desktop;

import com.credits.wallet.desktop.controller.AbstractController;
import com.credits.wallet.desktop.controller.FormDeinitializable;
import com.credits.wallet.desktop.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class for controlling navigation between vistas.
 * <p>
 * All methods on the navigator are static to facilitate
 * simple access from anywhere in the application.
 */
public class VistaNavigator {
    private final static Logger LOGGER = LoggerFactory.getLogger(VistaNavigator.class);

    /**
     * Convenience constants for fxml layouts managed by the navigator.
     */
    public static final String WELCOME = "/fxml/welcome.fxml";
    public static final String FORM_1 = "/fxml/generate_keys.fxml";
    public static final String FORM_4 = "/fxml/save_keys.fxml";
    public static final String FORM_5 = "/fxml/put_keys.fxml";
    public static final String WALLET = "/fxml/wallet.fxml";
    public static final String FORM_7 = "/fxml/generate_transaction.fxml";
    public static final String HEADER = "/fxml/header.fxml";
    public static final String HISTORY = "/fxml/history.fxml";
    public static final String MAIN = "/fxml/main.fxml";
    public static final String NEW_COIN = "/fxml/new_coin.fxml";
    public static final String SMART_CONTRACT = "/fxml/smart_contract.fxml";
    public static final String SMART_CONTRACT_DEPLOY = "/fxml/smart_contract_deploy.fxml";
    public static final String TRANSACTION = "/fxml/transaction.fxml";
    /**
     * The main application layout controller.
     */
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
     * <p>
     * Previously loaded vista for the same fxml file are not cached.
     * The fxml is loaded anew and a new vista node hierarchy generated
     * every time this method is invoked.
     * <p>
     * A more sophisticated load function could potentially add some
     * enhancements or optimizations, for example:
     * cache FXMLLoaders
     * cache loaded vista nodes, so they can be recalled or reused
     * allow a user to specify vista node reuse or new creation
     * allow back and forward history like a browser
     *
     * @param fxml the fxml file to be loaded.
     */
    public static void loadVista(String fxml, AbstractController oldVistaController, Map<String, Object> params) {
        changeVista(fxml, oldVistaController, params);
    }

    public static void loadVista(String fxml, AbstractController oldVistaController) {
        changeVista(fxml, oldVistaController, null);
    }

    private static void changeVista(String fxml, AbstractController oldVistaController, Map<String, Object> params) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(VistaNavigator.class.getResource(fxml));
            Node load = fxmlLoader.load();
            resizeForm((Pane) load);
            initialize(oldVistaController,fxmlLoader.getController(),params);
            deinitialize(oldVistaController);
            mainController.setVista(load);
        } catch (IOException e) {
            LOGGER.error("failed!", e);
        }
    }

    private static void initialize(AbstractController oldVistaController, AbstractController newVistaController, Map<String, Object> params) {
        try {
            if(oldVistaController!=null) {
                newVistaController.session = oldVistaController.session;
            }
            newVistaController.initializeHeader();
            newVistaController.initializeForm(params);
        } catch (Exception e) {
            LOGGER.error("Cannot initialize vista", e);
            throw e;
        }
    }

    private static void deinitialize(Object oldVistaController) {
        try {
            if (oldVistaController instanceof FormDeinitializable) {
                ((FormDeinitializable) oldVistaController).formDeinitialize();
            }
        } catch (Exception e) {
            LOGGER.error("Cannot formDeinitialize vista", e);
            throw e;
        }
    }

    public static void resizeForm(Pane pane) {
        pane.setPrefHeight(700D);
        pane.setPrefWidth(1300D);
    }
}