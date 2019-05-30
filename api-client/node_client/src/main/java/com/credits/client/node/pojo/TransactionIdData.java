package com.credits.client.node.pojo;

import java.io.Serializable;


public class TransactionIdData implements Serializable {

    private static final long serialVersionUID = -4240471143007475693L;
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
