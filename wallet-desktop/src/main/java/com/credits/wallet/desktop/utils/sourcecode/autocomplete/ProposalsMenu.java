package com.credits.wallet.desktop.utils.sourcecode.autocomplete;

import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

public class ProposalsMenu extends Popup {
    private ListView<ProposalItem> listView = new ListView();

    public ProposalsMenu() {
        super();
        listView.setMaxHeight(110);

        listView.addEventHandler(KeyEvent.KEY_PRESSED, (k) -> {
            if (k.getCode().equals(KeyCode.ESCAPE)) {
                this.clear();
                this.hide();
            }
        });

        listView.addEventHandler(KeyEvent.KEY_PRESSED, (k) -> {
            if (k.getCode().equals(KeyCode.ENTER)) {
                ProposalItem proposalItem = listView.getSelectionModel().getSelectedItem();
                proposalItem.action();
                this.hide();
            }
        });

        this.getContent().add(listView);
    }

    public void addItem(ProposalItem element) {
        listView.getItems().add(element);
    }

    public void clear() {
        listView.getItems().clear();
    }

    public boolean isEmpty() {
        return this.listView.getItems().isEmpty();
    }
}
