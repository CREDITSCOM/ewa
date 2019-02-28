package com.credits.general.pojo;

import java.util.Arrays;

public class ByteCodeObjectData {
    private final String name;
    private final byte[] byteCode;

    public ByteCodeObjectData(String name, byte[] byteCode) {
        this.name = name;
        this.byteCode = byteCode;
    }

    public String getName() {
        return name;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ByteCodeObjectData)) return false;

        ByteCodeObjectData that = (ByteCodeObjectData) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        return Arrays.equals(getByteCode(), that.getByteCode());
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + Arrays.hashCode(getByteCode());
        return result;
    }

    @Override
    public String toString() {
        return "ByteCodeObjectData{" +
            "name='" + name + '\'' +
            ", byteCode=" + Arrays.toString(byteCode) +
            '}';
    }

}
