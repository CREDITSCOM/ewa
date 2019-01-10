package com.credits.general.pojo;

public class ByteCodeObjectData {
    private final String name;
    private final byte[] bytecode;

    public ByteCodeObjectData(String name, byte[] bytecode) {
        this.name = name;
        this.bytecode = bytecode;
    }

    public String getName() {
        return name;
    }

    public byte[] getByteCode() {
        return bytecode;
    }
}
