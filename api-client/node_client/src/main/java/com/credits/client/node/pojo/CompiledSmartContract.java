package com.credits.client.node.pojo;


import java.io.Serializable;
import java.util.Objects;

public class CompiledSmartContract extends SmartContractData implements Serializable {
    private static final long serialVersionUID = 2675022641135493952L;

    private final SmartContractClass contractClass;

    public CompiledSmartContract(SmartContractData smartContractData, SmartContractClass contractClass) {
        super(
            smartContractData.getAddress(),
            smartContractData.getDeployer(),
            smartContractData.getSmartContractDeployData(),
            smartContractData.getObjectState());
        this.contractClass = contractClass;
    }

    public SmartContractClass getContractClass() {
        return contractClass;
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
        final StringBuilder sb = new StringBuilder("FavoriteSmartContract{");
        sb.append("contractClass=").append(contractClass);
        sb.append('}');
        return sb.toString();
    }
}
