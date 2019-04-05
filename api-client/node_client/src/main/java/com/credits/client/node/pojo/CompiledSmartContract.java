package com.credits.client.node.pojo;


import com.credits.general.pojo.ByteCodeObjectData;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CompiledSmartContract extends SmartContractData implements Serializable {
    private static final long serialVersionUID = 2675022641135493952L;
    private transient SmartContractClass contractClass;
    private final List<ByteCodeObjectData> byteCodeObjects;

    public CompiledSmartContract(
        SmartContractData smartContractData,
        SmartContractClass contractClass,
        List<ByteCodeObjectData> byteCodeObjectsData) {
        super(
            smartContractData.getAddress(),
            smartContractData.getDeployer(),
            smartContractData.getSmartContractDeployData(),
            smartContractData.getObjectState());
        this.contractClass = contractClass;
        this.byteCodeObjects = byteCodeObjectsData;
    }

    public SmartContractClass getContractClass() {
        return contractClass;
    }

    public void setContractClass(SmartContractClass contractClass) {
        this.contractClass = contractClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompiledSmartContract)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CompiledSmartContract that = (CompiledSmartContract) o;
        return Objects.equals(contractClass, that.contractClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contractClass);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CompiledSmartContract{");
        sb.append("contractClass=").append(contractClass);
        sb.append('}');
        return sb.toString();
    }
}
