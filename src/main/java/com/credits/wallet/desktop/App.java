package com.credits.wallet.desktop;


import com.credits.wallet.desktop.controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
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

            final Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            Scene scene = new Scene(pane, bounds.getWidth(), bounds.getHeight()*0.97);

            dialogStage.setScene(scene);
            dialogStage.setResizable(true);

            Controller controller = loader.getController();
            controller.setApp(this);

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
            e.printStackTrace();
        }
    }
}
