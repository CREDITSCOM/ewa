package com.credits.wallet.desktop;


import com.credits.wallet.desktop.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;


public class WalletApp extends Application {

    private final static Logger LOGGER = LoggerFactory.getLogger(WalletApp.class);
    AppStateInitializer appStateInitializer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        Properties prop = new Properties();
        URL url = WalletApp.class.getResource("/git.properties");
        String commit = null, author = null;
        if (url != null) {
            prop.load(url.openStream());
            commit = (String) prop.get("git.commit.id.abbrev");
            author = (String) prop.get("git.build.user.name");
        }
        LOGGER.info("\n\n\n");
        LOGGER.info("---------------------------------------------------------------------------");
        LOGGER.info("Starting Wallet app {}-{}", commit, author);
        LOGGER.info("---------------------------------------------------------------------------\n\n\n");
        appStateInitializer = appStateInitializer != null ? appStateInitializer : new AppStateInitializer();
        LOGGER.info("Initializing application state");
        appStateInitializer.init();
        LOGGER.info("Displaying the main window");

        stage.getIcons().add(new Image(WalletApp.class.getResourceAsStream("/img/icon.png")));
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(true);

        stage.setTitle("Credits");

        stage.setScene(createScene(loadMainPane()));
        loadFirstForm(VistaNavigator.WELCOME);
        //ScenicView.show(stage.getScene());
        stage.setOnCloseRequest(event -> {
            VistaNavigator.getCurrentVistaController().formDeinitialize();
            AppState.getSessionMap().forEach((account, session) -> session.close());
            Platform.exit();
            System.exit(0);
        });
        AppState.setPrimaryStage(stage);
        stage.show();
    }

    void loadFirstForm(String form) throws IOException {
        VistaNavigator.loadFirstForm(form);
    }

    private Pane loadMainPane() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        BorderPane mainPane = loader.load(WalletApp.class.getResourceAsStream(VistaNavigator.MAIN));
        MainController mainController = loader.getController();
        VistaNavigator.saveMainController(mainController);
        return mainPane;
    }


    private Scene createScene(Pane mainPane) {
        Scene scene = new Scene(mainPane);
        addFonts();
        scene.getStylesheets().setAll(WalletApp.class.getResource("/styles.css").toExternalForm());
        return scene;
    }


    private void addFonts() {
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Black.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-BlackItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Bold.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-BoldItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-ExtraBold.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-ExtraBoldItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-ExtraLight.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-ExtraLightItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Italic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Light.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-LightItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Medium.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-MediumItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Regular.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-SemiBold.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-SemiBoldItalic.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-Thin.otf").toExternalForm(), 14);
        Font.loadFont(WalletApp.class.getResource("/fonts/Montserrat-ThinItalic.otf").toExternalForm(), 14);
    }

}
