package com.credits.wallet.desktop;


import com.credits.wallet.desktop.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
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
            BorderPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            Controller controller = loader.getController();
            controller.setApp(this);

            boolean firstShow=false;
            if (currentStage==null) {
                currentStage = new Stage();
                firstShow=true;
            }
            currentStage.setTitle(title);
            currentStage.setScene(scene);

            if (firstShow)
                currentStage.showAndWait();

            if (currentStage!=null)
                currentStage.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
