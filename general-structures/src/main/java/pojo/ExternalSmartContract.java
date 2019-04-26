package pojo;

import pojo.apiexec.SmartContractGetResultData;

import java.io.Serializable;
import java.util.Objects;

public class ExternalSmartContract implements Serializable {
    private static final long serialVersionUID = -5560723251332545832L;
    private final SmartContractGetResultData contractData;
    private Object instance;

    public ExternalSmartContract(SmartContractGetResultData contractData) {
        this.contractData = contractData;
    }

    public SmartContractGetResultData getContractData() {
        return contractData;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalSmartContract)) {
            return false;
        }
        ExternalSmartContract that = (ExternalSmartContract) o;
        return Objects.equals(contractData, that.contractData) &&
            Objects.equals(instance, that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractData, instance);
    }

    @Override
    public String toString() {
        return "ExternalSmartContract{" + "contractData=" + contractData +
            ", instance=" + instance +
            '}';
    }
}
