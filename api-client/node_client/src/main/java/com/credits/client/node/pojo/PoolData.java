package com.credits.client.node.pojo;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class PoolData {

    private byte[] hash;
    private byte[] prevHash;
    private long time;
    private int transactionsCount;
    private long poolNumber;

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(byte[] prevHash) {
        this.prevHash = prevHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTransactionsCount() {
        return transactionsCount;
    }

    public void setTransactionsCount(int transactionsCount) {
        this.transactionsCount = transactionsCount;
    }

    public long getPoolNumber() {
        return poolNumber;
    }

    public void setPoolNumber(long poolNumber) {
        this.poolNumber = poolNumber;
    }
}
