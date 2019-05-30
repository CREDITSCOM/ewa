package com.credits.wallet.desktop.exception;


public class WalletDesktopException extends Exception {

    public WalletDesktopException(String errorMessage) {
        super(errorMessage);
    }

    public WalletDesktopException(Exception e) {
        super(e);
    }
}
