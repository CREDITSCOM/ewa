package com.credits.wallet.desktop;

import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.util.ObjectKeeper;
import com.credits.general.pojo.TransactionRoundData;
import com.credits.wallet.desktop.service.ContractInteractionService;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Session {
    public ObjectKeeper<ConcurrentHashMap<String, String>> coinsKeeper;
    public ObjectKeeper<HashMap<String, SmartContractData>> favoriteContractsKeeper;
    public String lastSmartContract;
    public String account;
    public ConcurrentHashMap<String, ConcurrentHashMap<Long, TransactionRoundData>> sourceMap = new ConcurrentHashMap<>();
    public ContractInteractionService contractInteractionService = initializeContractInteractionService();

    public ContractInteractionService initializeContractInteractionService() {
        return new ContractInteractionService();
    }
}
