package com.credits.wallet.desktop;

import com.credits.wallet.desktop.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class App extends Application {
    private Stage currentStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        showForm("/fxml/form0.fxml", "Wallet");
    }

    public void showForm(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource(fxmlFile));
            AnchorPane pane = (AnchorPane) loader.load();

            boolean firstShow=false;
            if (currentStage==null) {
                currentStage = new Stage();
                firstShow=true;
            }
            currentStage.setTitle(title);
            Scene scene = new Scene(pane);
            currentStage.setScene(scene);

            Controller controller = loader.getController();
            controller.setApp(this);

            if (firstShow)
                currentStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
