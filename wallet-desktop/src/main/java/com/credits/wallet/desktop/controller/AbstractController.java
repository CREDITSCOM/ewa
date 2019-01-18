package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.Session;
import javafx.fxml.FXML;

public abstract class AbstractController implements FormInitializable, FormDeinitializable {
    public Session session;

    @FXML
    private HeaderController headerController;

    public void initializeHeader() {
        if(headerController!=null) {
            headerController.parentController = this;
            headerController.session = this.session;
        }
    }

    public void closeSession() {
        if(session.favoriteContractsKeeper != null){
            session.favoriteContractsKeeper.flush();
        }
        if(session.coinsKeeper != null){
            session.coinsKeeper.flush();
        }

        session = null;
    }
}
