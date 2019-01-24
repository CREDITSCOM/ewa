package com.credits.wallet.desktop;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.util.ObjectKeeper;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.wallet.desktop.service.ContractInteractionService;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    public ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper;
    public ObjectKeeper<HashMap<String, SmartContractData>> favoriteContractsKeeper;
    public String lastSmartContract;
    public String account;
    public ConcurrentHashMap<Long, TransactionRoundData> sourceMap = new ConcurrentHashMap<>();
    public ContractInteractionService contractInteractionService = initializeContractInteractionService();

    public ContractInteractionService initializeContractInteractionService() {
        return new ContractInteractionService(this);
    }
    public ObservableList<DeploySmartListItem> deploySmartListItems;

    public int lastSmartIndex =0;

    public void close() {
        if(this.favoriteContractsKeeper != null){
            this.favoriteContractsKeeper.flush();
        }
        if(this.coinsKeeper != null){
            this.coinsKeeper.flush();
        }
    }
}
