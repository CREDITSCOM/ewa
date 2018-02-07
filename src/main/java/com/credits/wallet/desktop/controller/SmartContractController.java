package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.Utils;
import javafx.fxml.FXML;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Random;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller {
    @FXML
    private void handleBack() {
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleDeploy() {
        char[] characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb=new StringBuilder();
        sb.append("CST");
        Random random=new Random();
        int max=characters.length-1;
        for (int i=0; i<29; i++)
            sb.append(characters[random.nextInt(max)]);

        String token=sb.toString();

        StringSelection selection = new StringSelection(token);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        Utils.showInfo("Token\n\n"+token+"\n\nhas generated and copied to clipboard");
    }
}
