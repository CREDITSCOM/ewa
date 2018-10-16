package com.credits.wallet.desktop.struct;

import javafx.scene.control.ToggleButton;

import java.io.Serializable;

public class SmartContractTabRow implements Serializable {

    private static final long serialVersionUID = 4111650022718657167L;

    private String id;
    private ToggleButton fav;

    public SmartContractTabRow(String label, ToggleButton fav1) {
        this.id = label;
        this.fav = fav1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ToggleButton getFav() {
        return fav;
    }

    public void setFav(ToggleButton fav) {
        this.fav = fav;
    }
}
