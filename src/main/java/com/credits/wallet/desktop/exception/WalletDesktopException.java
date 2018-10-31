package com.credits.wallet.desktop.exception;

/**
 * Created by Rustem.Saidaliyev on 29.01.2018.
 */
public class WalletDesktopException extends Exception {

    public WalletDesktopException(String errorMessage) {
        super(errorMessage);
    }

    public WalletDesktopException(Exception e) {
        super(e);
    }
}
