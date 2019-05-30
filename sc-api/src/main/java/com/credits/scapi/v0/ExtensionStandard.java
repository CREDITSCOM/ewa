package com.credits.scapi.v0;

public interface ExtensionStandard extends BasicStandard {

    void register();

    boolean buyTokens(String amount);
}
