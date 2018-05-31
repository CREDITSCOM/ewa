package com.credits.wallet.desktop;


import javafx.application.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class App extends Application {

    private static Stage currentStage;

    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        LOGGER.info("Starting Wallet app");
        AppStateInitializer appStateInitializer = new AppStateInitializer();
        LOGGER.info("Initializing application state");
        appStateInitializer.init();
        LOGGER.info("Displaying the main window");
        showForm("/fxml/form0.fxml", "Wallet");
    }

    public static void showForm(String fxmlFile, String title) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource(fxmlFile));

        AnchorPane pane;
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

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
    }

    @Override
    public void stop() {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
    }
}
