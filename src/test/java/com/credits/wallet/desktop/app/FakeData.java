package com.credits.wallet.desktop.app;

import com.credits.common.utils.Converter;
import com.credits.leveldb.client.data.SmartContractData;
import com.credits.leveldb.client.data.TransactionData;
import com.credits.leveldb.client.thrift.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;

public class FakeData {
    public static ArrayList<SmartContractData> getSmartContractData() {
        ArrayList<SmartContractData> smartContractData = new ArrayList<>();
        try {
            SmartContractData smartContract1 =
                new SmartContractData(Converter.decodeFromBASE58("11111111111111111111111111111111111111111111"),
                    Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"),
                    "public class Contract extends SmartContract {\n" + "\n" + " public Contract() {\n" +
                        " total = 0;\n" + " }\n" + "}", Converter.decodeFromBASE58(
                    "2d2hvgkhApbcfvJT6eUVH8t3MpV5pvT24CaZG1ughCs9YJnvAQK6TMHMt4JVGXTK8Y4HChwN1eTukJqxNfhP84A2RCM3Gp77pQUpTPTYSdofkMGmZ8npzVroB3hgs7j8SXpCjTs6gQJgbFt4hXBkAZXSbeHnT5cH8sYFpyBE2F1jX9TFP8KtnyoXzsqT2RYDquCAUWAvZjjkedqxRNMa2uTv3kQj7n9X8ADktA7jvzHQKBTkMLym8BJvNUKGLCBfuTd4rRFAbKrNC8VV1bYDPgGeW1NFaESwF5eqThq7zUNMR4Kj"),
                    "189B0DA9BA1B0FC2676C00A61D8338C4");
            SmartContractData smartContract2 =
                new SmartContractData(Converter.decodeFromBASE58("22222222222222222222222222222222222222222222"),
                    Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"),
                    "public class Contract extends SmartContract { final String owner ; private String name;  private String symbol;  private long totalCoins;  private java.util.Map<String, java.util.Map<String, Long>> allowed;  java.util.Map<String, Long> balances;  private final long tokenCost; public Contract() { name = \"CreditsToken\"; symbol = \"CST\"; totalCoins = 10_000_000; tokenCost = 1000; owner = \"G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ\"; allowed = new java.util.HashMap<>(); balances = new java.util.HashMap<>(); balances.put(owner, 1_000_000L); } public String getName() { return name; } public String getSymbol() { return symbol; } public long totalSupply() { return totalCoins; } public long balanceOf(String owner) { Long balance = balances.get(owner); return balance != null ? balance : 0; } public long allowance(String owner, String spender) { if(allowed.get(owner) == null) return 0; Long amount = allowed.get(owner).get(spender); return amount != null ? amount : 0; } public boolean transfer(String to, long amount) { Long senderBalance = balances.get(initiator); Long targetBalance = balances.get(to); if (senderBalance == null || targetBalance == null || senderBalance < amount) { return false; } balances.put(initiator, senderBalance - amount); balances.put(to, targetBalance + amount); return true; } public boolean transferFrom(String from, String to, long amount) { java.util.Map<String, Long> spender = allowed.get(from); if (spender == null) { return false; } Long allowTokens = spender.get(initiator); if (allowTokens == null || allowTokens < amount) { return false; } spender.put(initiator, allowTokens - amount); Long sourceBalance = balances.get(from); Long targetBalance = balances.get(to); if (sourceBalance == null || targetBalance == null || sourceBalance < amount) { return false; } balances.put(from, sourceBalance - amount); balances.put(to, targetBalance + amount); return true; } public void approve(String spender, long amount) { java.util.Map<String, Long> initiatorSpenders = allowed.get(initiator); if (initiatorSpenders == null) { java.util.Map<String, Long> newSpender = new java.util.HashMap<>(); newSpender.put(spender, amount); allowed.put(initiator, newSpender); } else { Long spenderAmount = initiatorSpenders.get(spender); initiatorSpenders.put(spender, spenderAmount == null ? amount : spenderAmount + amount); } } public void buyTokens(long amount) { sendTransaction(initiator, owner, tokenCost * amount, \"\", 0.1); balances.put(initiator,amount); }}",
                    Converter.decodeFromBASE58(
                        "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"),
                    "9318E3272F16846432001D1FD96BA851");
            ;
            SmartContractData smartContract3 =
                new SmartContractData(Converter.decodeFromBASE58("33333333333333333333333333333333333333333333"),
                    Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"),
                    "public class Contract extends SmartContract { final String owner ; private String name;  private String symbol;  private long totalCoins;  private java.util.Map<String, java.util.Map<String, Long>> allowed;  java.util.Map<String, Long> balances;  private final long tokenCost; public Contract() { name = \"CreditsToken\"; symbol = \"CST\"; totalCoins = 10_000_000; tokenCost = 1000; owner = \"G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ\"; allowed = new java.util.HashMap<>(); balances = new java.util.HashMap<>(); balances.put(owner, 1_000_000L); } public String getName() { return name; } public String getSymbol() { return symbol; } public long totalSupply() { return totalCoins; } public long balanceOf(String owner) { Long balance = balances.get(owner); return balance != null ? balance : 0; } public long allowance(String owner, String spender) { if(allowed.get(owner) == null) return 0; Long amount = allowed.get(owner).get(spender); return amount != null ? amount : 0; } public boolean transfer(String to, long amount) { Long senderBalance = balances.get(initiator); Long targetBalance = balances.get(to); if (senderBalance == null || targetBalance == null || senderBalance < amount) { return false; } balances.put(initiator, senderBalance - amount); balances.put(to, targetBalance + amount); return true; } public boolean transferFrom(String from, String to, long amount) { java.util.Map<String, Long> spender = allowed.get(from); if (spender == null) { return false; } Long allowTokens = spender.get(initiator); if (allowTokens == null || allowTokens < amount) { return false; } spender.put(initiator, allowTokens - amount); Long sourceBalance = balances.get(from); Long targetBalance = balances.get(to); if (sourceBalance == null || targetBalance == null || sourceBalance < amount) { return false; } balances.put(from, sourceBalance - amount); balances.put(to, targetBalance + amount); return true; } public void approve(String spender, long amount) { java.util.Map<String, Long> initiatorSpenders = allowed.get(initiator); if (initiatorSpenders == null) { java.util.Map<String, Long> newSpender = new java.util.HashMap<>(); newSpender.put(spender, amount); allowed.put(initiator, newSpender); } else { Long spenderAmount = initiatorSpenders.get(spender); initiatorSpenders.put(spender, spenderAmount == null ? amount : spenderAmount + amount); } } public void buyTokens(long amount) { sendTransaction(initiator, owner, tokenCost * amount, \"\", 0.1); balances.put(initiator,amount); }}",
                    Converter.decodeFromBASE58(
                        "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"),
                    "18DD89BF56F7385B3524C26F117D4AB6");
            smartContractData.add(smartContract1);
            smartContractData.add(smartContract2);
            smartContractData.add(smartContract3);

        } catch (Exception ignored) {

        }
        return smartContractData;
    }

    public static ArrayList<TransactionData> getTransactionDataList() {
        ArrayList<TransactionData> transactionData = new ArrayList<>();
        try {

        TransactionData transactionData1 = new TransactionData();
        TransactionData transactionData2 = new TransactionData();
        TransactionData transactionData3 = new TransactionData();
        TransactionData transactionData4 = new TransactionData();
        TransactionData transactionData5 = new TransactionData();
        TransactionData transactionData6 = new TransactionData();
        TransactionData transactionData7 = new TransactionData();
        TransactionData transactionData8 = new TransactionData();
        TransactionData transactionData9 = new TransactionData();
        transactionData1.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.1");
        transactionData2.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.2");
        transactionData3.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.3");
        transactionData4.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.4");
        transactionData5.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.5");
        transactionData6.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.6");
        transactionData7.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.7");
        transactionData8.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.8");
        transactionData9.setId("91693ec6ab3ffa2f0e887cda912c0f7d6bae63dac38ca1eb01b35fa43c5cfbd7.9");
        transactionData1.setAmount(new BigDecimal(1.0));
        transactionData2.setAmount(new BigDecimal(1.0));
        transactionData3.setAmount(new BigDecimal(1.0));
        transactionData4.setAmount(new BigDecimal(1.0));
        transactionData5.setAmount(new BigDecimal(1.0));
        transactionData6.setAmount(new BigDecimal(1.0));
        transactionData7.setAmount(new BigDecimal(1.0));
        transactionData8.setAmount(new BigDecimal(1.0));
        transactionData9.setAmount(new BigDecimal(2147483647.0));
        transactionData1.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData2.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData3.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData4.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData5.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData6.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData7.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData8.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData9.setSource(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData1.setTarget(Converter.decodeFromBASE58("CRs6AbAdR8XpsJVLNGRC8yFubPgvZmPwHeiBNMHK7tMV"));
        transactionData2.setTarget(Converter.decodeFromBASE58("CRs6AbAdR8XpsJVLNGRC8yFubPgvZmPwHeiBNMHK7tMV"));
        transactionData3.setTarget(Converter.decodeFromBASE58("CRs6AbAdR8XpsJVLNGRC8yFubPgvZmPwHeiBNMHK7tMV"));
        transactionData4.setTarget(Converter.decodeFromBASE58("777EN2pQ4wh9XuGneMD1h2Lme2TZmzNv7rUa9uZLz8CM"));
        transactionData5.setTarget(Converter.decodeFromBASE58("CRs6AbAdR8XpsJVLNGRC8yFubPgvZmPwHeiBNMHK7tMV"));
        transactionData6.setTarget(Converter.decodeFromBASE58("BqN3YEfYvJh2JfaQZWcmV9aByikt1DZERgMXuKVdtqBE"));
        transactionData7.setTarget(Converter.decodeFromBASE58("CRs6AbAdR8XpsJVLNGRC8yFubPgvZmPwHeiBNMHK7tMV"));
        transactionData8.setTarget(Converter.decodeFromBASE58("CRs6AbAdR8XpsJVLNGRC8yFubPgvZmPwHeiBNMHK7tMV"));
        transactionData9.setTarget(Converter.decodeFromBASE58("G2iSMjqaEQmA5pvFuFjKbMqJUxJZceAY5oc1uotr7SZZ"));
        transactionData.add(transactionData1);
        transactionData.add(transactionData2);
        transactionData.add(transactionData3);
        transactionData.add(transactionData4);
        transactionData.add(transactionData5);
        transactionData.add(transactionData6);
        transactionData.add(transactionData7);
        transactionData.add(transactionData8);
        transactionData.add(transactionData9);

        } catch (Exception ignored) {

        }

        return transactionData;
    }
}