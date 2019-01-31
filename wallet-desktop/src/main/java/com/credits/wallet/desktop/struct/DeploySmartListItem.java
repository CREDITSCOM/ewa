package com.credits.wallet.desktop.struct;

import java.io.Serializable;

public class DeploySmartListItem implements Serializable {

    private static final long serialVersionUID = 4111650022718657168L;

    public String name;
    public String sourceCode;
    public ItemState state;

    public DeploySmartListItem(String sourceCode, String name, ItemState state) {
        this.sourceCode = sourceCode;
        this.name = name;
        this.state = state;
    }


    public enum ItemState {
        NEW, DELETED, SAVED
    }

    @Override
    public String toString() {
        return name;
    }
}