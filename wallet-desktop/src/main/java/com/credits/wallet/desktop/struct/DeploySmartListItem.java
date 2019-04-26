package com.credits.wallet.desktop.struct;

import java.io.Serializable;

public class DeploySmartListItem implements Serializable {

    private static final long serialVersionUID = 4111650022718657168L;

    public String name;
    public String sourceCode;
    public String testSourceCode;
    public ItemState state;

    public DeploySmartListItem(String sourceCode, String testSourceCode, String name, ItemState state) {
        this.sourceCode = sourceCode;
        this.name = name;
        this.state = state;
        this.testSourceCode = testSourceCode;
    }


    public enum ItemState {
        NEW, SAVED
    }

    @Override
    public String toString() {
        return name;
    }
}