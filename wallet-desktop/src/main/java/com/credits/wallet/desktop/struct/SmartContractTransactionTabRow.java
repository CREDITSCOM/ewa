package com.credits.wallet.desktop.struct;

import com.credits.general.pojo.VariantData;

/**
 * Created by Rustem Saidaliyev on 25.06.2018.
 */
public class SmartContractTransactionTabRow extends TransactionTabRow {

    private VariantData returnedValue;

    public VariantData getReturnedValue() {
        return returnedValue;
    }

    public void setReturnedValue(VariantData returnedValue) {
        this.returnedValue = returnedValue;
    }
}
