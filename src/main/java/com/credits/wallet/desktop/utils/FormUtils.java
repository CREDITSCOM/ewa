package com.credits.wallet.desktop.utils;

import com.credits.wallet.desktop.AppState;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.StageStyle;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class FormUtils {

    public static void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Error");
        alert.setHeaderText("Error!");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void showInfo(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Information");
        alert.setHeaderText("Information");
        alert.setContentText(text);
        alert.showAndWait();
    }
    public static void showPlatformWarning(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Warning");
            alert.setHeaderText("Warning");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showPlatformError(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showPlatformInfo(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Info");
            alert.setHeaderText("Info");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void resizeForm(Pane pane) {
/*
        pane.setPrefHeight(AppState.screenHeight*0.7);
        pane.setPrefWidth(AppState.screenWidth*0.7);
 */
        pane.setPrefHeight(740D);
        pane.setPrefWidth(1370D);



    }

}

