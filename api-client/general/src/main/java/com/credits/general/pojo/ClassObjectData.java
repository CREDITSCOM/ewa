package com.credits.general.pojo;

import java.util.Arrays;
import java.util.List;

public class ClassObjectData {

    public List<ByteCodeObjectData> byteCodeObjects;
    public byte[] instance;

    public ClassObjectData(List<ByteCodeObjectData> byteCodeObjects, byte[] instance) {
        this.byteCodeObjects = byteCodeObjects;
        this.instance = instance;
    }

    public List<ByteCodeObjectData> getByteCodeObjects() {
        return byteCodeObjects;
    }

    public void setByteCodeObjects(List<ByteCodeObjectData> byteCodeObjects) {
        this.byteCodeObjects = byteCodeObjects;
    }

    public byte[] getInstance() {
        return instance;
    }

    public void setInstance(byte[] instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassObjectData)) return false;

        ClassObjectData that = (ClassObjectData) o;

        if (getByteCodeObjects() != null ? !getByteCodeObjects().equals(that.getByteCodeObjects()) : that.getByteCodeObjects() != null)
            return false;
        return Arrays.equals(getInstance(), that.getInstance());
    }

    @Override
    public int hashCode() {
        int result = getByteCodeObjects() != null ? getByteCodeObjects().hashCode() : 0;
        result = 31 * result + Arrays.hashCode(getInstance());
        return result;
    }

    @Override
    public String toString() {
        return "ClassObjectData{" +
                "byteCodeObjects=" + byteCodeObjects +
                ", instance=" + Arrays.toString(instance) +
                '}';
    }
}
