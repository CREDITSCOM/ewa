package com.credits.wallet.desktop.utils.sourcecode.codeArea;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class CreditsToolboxPopup extends Popup {
    public CreditsToolboxPopup(CreditsCodeArea codeArea, boolean readOnly) {
        super();
        this.setAutoHide(true);
        Button cut = new Button("Cut");
        cut.setPrefSize(80, 15);
        cut.setDisable(readOnly);
        cut.setOnAction(e -> {
            codeArea.cut();
            this.hide();
        });
        Button copy = new Button("Copy");
        copy.setPrefSize(80, 15);
        copy.setOnAction(e -> {
            codeArea.copy();
            this.hide();
        });
        Button paste = new Button("Paste");
        paste.setPrefSize(80, 15);
        paste.setDisable(readOnly);
        paste.setOnAction(e -> {
            codeArea.replaceTabSymbolInClipboard();
            this.hide();
        });
        Button select = new Button("Select All");
        select.setPrefSize(80, 15);
        select.setOnAction(e -> {
            codeArea.selectAll();
            this.hide();
        });
        VBox box = new VBox();
        box.getStyleClass().add("credits-popup-toolbox");
        box.setPrefSize(80, 60);
        box.getChildren().addAll(cut, copy, paste, select);

        this.getContent().add(box);

        this.focusedProperty().addListener((observable, old, newPropertyValue) -> {
            if (!newPropertyValue) {
                this.hide();
            }
        });


        codeArea.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                this.setAutoFix(false);
                this.show(codeArea, event.getScreenX(), event.getScreenY());
            } else if (this.isShowing() && event.getClickCount() == 1) {
                this.hide();
            }
        });
    }
}
