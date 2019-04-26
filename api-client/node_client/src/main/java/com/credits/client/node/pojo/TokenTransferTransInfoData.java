package com.credits.client.node.pojo;

import java.util.Arrays;

public class TokenTransferTransInfoData extends SmartTransInfoData {
    private String code;
    private byte[] sender;
    private byte[] receiver;
    private String amount;

    public TokenTransferTransInfoData(String code, byte[] sender, byte[] receiver, String amount) {
        this.code = code;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    public byte[] getReceiver() {
        return receiver;
    }

    public void setReceiver(byte[] receiver) {
        this.receiver = receiver;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenTransferTransInfoData)) return false;

        TokenTransferTransInfoData that = (TokenTransferTransInfoData) o;

        if (getCode() != null ? !getCode().equals(that.getCode()) : that.getCode() != null) return false;
        if (!Arrays.equals(getSender(), that.getSender())) return false;
        if (!Arrays.equals(getReceiver(), that.getReceiver())) return false;
        return getAmount() != null ? getAmount().equals(that.getAmount()) : that.getAmount() == null;
    }

    @Override
    public int hashCode() {
        int result = getCode() != null ? getCode().hashCode() : 0;
        result = 31 * result + Arrays.hashCode(getSender());
        result = 31 * result + Arrays.hashCode(getReceiver());
        result = 31 * result + (getAmount() != null ? getAmount().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TokenTransferTransInfoData{" +
                "code='" + code + '\'' +
                ", sender=" + Arrays.toString(sender) +
                ", receiver=" + Arrays.toString(receiver) +
                ", amount='" + amount + '\'' +
                '}';
    }
}
