package com.credits.wallet.desktop.struct;

public class DeploySmartListItem {
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