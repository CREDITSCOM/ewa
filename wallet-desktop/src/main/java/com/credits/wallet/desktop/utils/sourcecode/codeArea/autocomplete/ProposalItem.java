package com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete;

import java.util.function.Consumer;

public class ProposalItem {
    private Object autocompletionText;
    private String displayText;
    private Consumer<String> actionHandler;

    public ProposalItem(Object autocompletionText, String displayText) {
        this.autocompletionText = autocompletionText;
        this.displayText = displayText;
    }

    public void setActionHandler(Consumer<String> actionHandler) {
        this.actionHandler = actionHandler;
    }

    public void action() {
        if (autocompletionText != null) {
            actionHandler.accept((String) this.autocompletionText);
        }
    }

    @Override
    public String toString() {
        return displayText;
    }
}
