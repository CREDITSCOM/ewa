package com.credits.wallet.desktop;

import com.credits.wallet.desktop.controller.Controller;
import com.credits.wallet.desktop.controller.Form0Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        showForm("/fxml/form0.fxml", "Wallet", null);
    }

    public void showForm(String fxmlFile, String title, Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource(fxmlFile));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            Controller controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setApp(this);

            if (currentStage!=null)
                currentStage.close();

            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
