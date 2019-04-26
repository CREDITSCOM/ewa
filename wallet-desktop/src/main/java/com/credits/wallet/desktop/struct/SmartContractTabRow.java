package com.credits.wallet.desktop.struct;

import com.credits.client.node.pojo.CompiledSmartContract;
import javafx.scene.control.ToggleButton;

public class SmartContractTabRow {

    private String id;
    private ToggleButton fav;
    private CompiledSmartContract compiledSmartContract;

    public SmartContractTabRow(String id, ToggleButton fav, CompiledSmartContract smartContract) {
        this.id = id;
        this.fav = fav;
        this.compiledSmartContract = smartContract;
    }

    public SmartContractTabRow(String label, ToggleButton fav1) {
        this.id = label;
        this.fav = fav1;
    }

    public CompiledSmartContract getCompiledSmartContract() {
        return compiledSmartContract;
    }

    public void setCompiledSmartContract(CompiledSmartContract compiledSmartContract) {
        this.compiledSmartContract = compiledSmartContract;
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
