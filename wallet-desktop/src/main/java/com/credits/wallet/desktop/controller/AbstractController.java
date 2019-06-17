package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Session;
import javafx.fxml.FXML;

public abstract class AbstractController implements FormInitializable, FormDeinitializable {
    public Session session;

    @FXML
    private HeaderController headerController;

    public void closeSession() {
        session.close();
        session = null;
    }

    protected void setSession(String pubKey) {
        if(AppState.getSessionMap().get(pubKey)!=null) {
            this.session = AppState.getSessionMap().get(pubKey);
        } else {
            this.session = new Session(pubKey);
        }
    }
}
