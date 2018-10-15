package com.credits.wallet.desktop;


import com.credits.wallet.desktop.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class App extends Application {

    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);
    AppStateInitializer appStateInitializer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("Starting Wallet app");
        appStateInitializer = appStateInitializer != null ? appStateInitializer : new AppStateInitializer();
        LOGGER.info("Initializing application state");
        appStateInitializer.init();
        LOGGER.info("Displaying the main window");

        stage.getIcons().add(new Image(App.class.getResourceAsStream("/img/icon.png")));
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        AppState.screenHeight = bounds.getHeight();
        AppState.screenWidth = bounds.getWidth();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(true);

        stage.setTitle("Credits");

        stage.setScene(
            createScene(
                loadMainPane()
            )
        );
        stage.show();
/*
        ScenicView.show(stage.getScene());
*/
    }

    private Pane loadMainPane() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        Pane mainPane = loader.load(
            App.class.getResourceAsStream(VistaNavigator.MAIN)
        );
        MainController mainController = loader.getController();

        VistaNavigator.setMainController(mainController);
        AppState.account="GWe8WZYLBxAqsfPZgejnysXQm5Q697VSsyr3x59RvYBf";
        VistaNavigator.loadVista(VistaNavigator.WALLET);

        return mainPane;
    }


    private Scene createScene(Pane mainPane) {
        Scene scene = new Scene(
            mainPane
        );

        scene.getStylesheets().setAll(
            App.class.getResource("/styles.css").toExternalForm()
        );

        return scene;
    }

    @Override
    public void stop() {
        if (AppState.executor != null) {
            AppState.executor.shutdown();
        }
    }
}
