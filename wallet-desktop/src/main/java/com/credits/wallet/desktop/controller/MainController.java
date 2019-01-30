package com.credits.wallet.desktop.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/**
 * Main controller class for the entire layout.
 */
public class MainController {

    @FXML
    private BorderPane vistaHolder;

    public void setTopVista(Node node) {
        vistaHolder.setTop(node);
    }

    public void setVista(Node node) {
        vistaHolder.setCenter(node);
    }
}