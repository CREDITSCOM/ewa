package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import javafx.stage.Stage;

/**
 * Created by goncharov-eg on 18.01.2018.
 */
public class Controller {
    Stage dialogStage;
    App app;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setApp(App app) {
        this.app = app;
    }
}
