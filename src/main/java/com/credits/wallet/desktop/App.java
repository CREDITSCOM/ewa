package com.credits.wallet.desktop;


import com.credits.leveldb.client.*;
import com.credits.wallet.desktop.utils.Utils;
import javafx.application.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class App extends Application {
    private static final String ERR_NO_PROPERTIES =
            "File settings.properties not found";
    private static final String ERR_NO_API_ADDR =
            "The server address could not be determined. Check api.addr parameter in the settings.properties file";
    private static final String ERR_NO_CONTRACT_EXECUTOR =
            "Parameters for java contract executor could not be determined. Check contract.executor.host, contract.executor.port, contract.executor.dir  parameters in the settings.properties file";
    private static Stage currentStage;

    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Locale loc = Locale.getDefault();
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
            char sep = symbols.getDecimalSeparator();
            AppState.decSep = Character.toString(sep);

            FileInputStream fis = new FileInputStream("settings.properties");
            Properties property = new Properties();
            property.load(fis);

            AppState.creditMonitorURL=property.getProperty("creditmonitor.url");

            String apiAddr = property.getProperty("api.addr");
            String apiPort = property.getProperty("api.port");
            AppState.contractExecutorHost = property.getProperty("contract.executor.host");
            try {
                AppState.contractExecutorPort = Integer.valueOf(property.getProperty("contract.executor.port"));
            } catch (Exception e) {
                // do nothing
            }

            if (apiAddr == null || apiAddr.isEmpty() || apiPort == null || apiPort.isEmpty()) {
                Utils.showError(ERR_NO_API_ADDR);
            } else if (AppState.contractExecutorHost == null ||
                    AppState.contractExecutorPort == null) {
                Utils.showError(ERR_NO_CONTRACT_EXECUTOR);
            } else {
                AppState.apiClient = ApiClient.getInstance(apiAddr, Integer.valueOf(apiPort));
                showForm("/fxml/form0.fxml", "Wallet");
            }

        } catch (Exception e) {
            LOGGER.error(ERR_NO_PROPERTIES, e);
            Utils.showError(ERR_NO_PROPERTIES);
        }
    }

    public static void showForm(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource(fxmlFile));
            AnchorPane pane = loader.load();

            final Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene;
            if (currentStage == null) {
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getVisualBounds();
                scene = new Scene(pane, bounds.getWidth(), bounds.getHeight() * 0.97);
            } else {
                scene = new Scene(pane, currentStage.getScene().getWidth(), currentStage.getScene().getHeight());
            }

            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            boolean firstShow = false;
            if (currentStage == null) {
                currentStage = new Stage();
                firstShow = true;
            }
            currentStage.setTitle(title);
            currentStage.setScene(scene);

            if (firstShow) {
                currentStage.showAndWait();
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (AppState.executor!=null)
            AppState.executor.shutdown();
    }}
