package com.credits.wallet.desktop.struct;

import com.credits.general.pojo.SmartContractData;
import javafx.scene.control.ToggleButton;

import java.io.Serializable;

public class SmartContractTabRow implements Serializable {

    private static final long serialVersionUID = 4111650022718657167L;

    private String id;
    private ToggleButton fav;
    private SmartContractData smartContractData;

    public SmartContractTabRow(String id, ToggleButton fav, SmartContractData smartContractData) {
        this.id = id;
        this.fav = fav;
        this.smartContractData = smartContractData;
    }

    public SmartContractTabRow(String label, ToggleButton fav1) {
        this.id = label;
        this.fav = fav1;
    }

    public SmartContractData getSmartContractData() {
        return smartContractData;
    }

    public void setSmartContractData(SmartContractData smartContractData) {
        this.smartContractData = smartContractData;
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
