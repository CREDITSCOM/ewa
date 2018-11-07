package com.credits.wallet.desktop.struct;


import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoinTabRow implements Serializable, Comparable<CoinTabRow> {

    private static final long serialVersionUID = 4222650022718657167L;

    private String name;
    private String balance;
    private String smartName;
    private Lock lock = new ReentrantLock();


    public CoinTabRow(String coinName, String value, String smartContractAddress) {
        name = coinName;
        balance = value;
        smartName = smartContractAddress;
    }


    public CoinTabRow() {
    }

    public Lock getLock() {
        return lock;
    }

    public String getSmartName() {
        return smartName;
    }

    public void setSmartName(String smartName) {
        this.smartName = smartName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBalance() {
        return balance;
    }

    public void setValue(String balance) {
        this.balance = balance;
    }

    public int compareTo(CoinTabRow coinTabRow) {
        return name.compareTo(coinTabRow.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CoinTabRow)) {
            return false;
        }
        CoinTabRow that = (CoinTabRow) o;
        return Objects.equals(name, that.name) && Objects.equals(balance, that.balance) &&
            Objects.equals(smartName, that.smartName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, balance, smartName);
    }
}
