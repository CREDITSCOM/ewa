package com.credits.general.client.node.pojo;

/**
 * Created by Rustem.Saidaliyev on 30.08.2018.
 */
public class TransactionIdData {

    public byte[] poolHash;
    public int index;

    public TransactionIdData() {
    }

    public TransactionIdData(byte[] poolHash, int index) {
        this.poolHash = poolHash;
        this.index = index;
    }

    public byte[] getPoolHash() {
        return poolHash;
    }

    public void setPoolHash(byte[] poolHash) {
        this.poolHash = poolHash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
