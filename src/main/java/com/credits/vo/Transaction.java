package com.credits.vo;

public class Transaction {

    private String id;
    private long value;
    private char operation;

    public Transaction(String id, long value, char operation) {
        this.id = id;
        this.value = value;
        this.operation = operation;
    }

    public Transaction() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public char getOperation() {
        return operation;
    }

    public void setOperation(char operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", value=" + value +
                ", operation=" + operation +
                '}';
    }
}
