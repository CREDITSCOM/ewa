package com.credits.wallet.desktop.struct;

import com.credits.client.node.pojo.SmartTransInfoData;

/**
 * Created by Rustem Saidaliyev on 25.06.2018.
 */
public class SmartContractTransactionTabRow extends TransactionTabRow {

    private SmartTransInfoData smartInfo;

    public SmartTransInfoData getSmartInfo() {
        return smartInfo;
    }

    public void setSmartInfo(SmartTransInfoData smartInfo) {
        this.smartInfo = smartInfo;
    }
}
