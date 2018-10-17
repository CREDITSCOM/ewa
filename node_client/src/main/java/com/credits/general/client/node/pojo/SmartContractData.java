package com.credits.general.client.node.pojo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rustem Saidaliyev on 16.05.2018.
 */
public class SmartContractData implements Serializable {

    private byte[] address;
    private byte[] deployer;
    private String sourceCode;
    private byte[] byteCode;
    private String hashState;
    private byte[] objectState;
    private String method;
    private List<String> params;

    public SmartContractData(byte[] address, byte[] deployer, String sourceCode, byte[] byteCode, String hashState,
        byte[] objectState) {
        this.address = address;
        this.deployer = deployer;
        this.sourceCode = sourceCode;
        this.byteCode = byteCode;
        this.hashState = hashState;
        this.objectState = objectState;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public byte[] getObjectState() {
        return objectState;
    }

    public void setObjectState(byte[] objectState) {
        this.objectState = objectState;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public byte[] getDeployer() {
        return deployer;
    }

    public void setDeployer(byte[] deployer) {
        this.deployer = deployer;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public void setByteCode(byte[] byteCode) {
        this.byteCode = byteCode;
    }

    public String getHashState() {
        return hashState;
    }

    public void setHashState(String hashState) {
        this.hashState = hashState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SmartContractData that = (SmartContractData) o;

        if (!Arrays.equals(address, that.address)) {
            return false;
        }
        if (!Arrays.equals(deployer, that.deployer)) {
            return false;
        }
        if (sourceCode != null ? !sourceCode.equals(that.sourceCode) : that.sourceCode != null) {
            return false;
        }
        if (!Arrays.equals(byteCode, that.byteCode)) {
            return false;
        }
        if (hashState != null ? !hashState.equals(that.hashState) : that.hashState != null) {
            return false;
        }
        if (!Arrays.equals(objectState, that.objectState)) {
            return false;
        }
        if (method != null ? !method.equals(that.method) : that.method != null) {
            return false;
        }
        return params != null ? params.equals(that.params) : that.params == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(address);
        result = 31 * result + Arrays.hashCode(deployer);
        result = 31 * result + (sourceCode != null ? sourceCode.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(byteCode);
        result = 31 * result + (hashState != null ? hashState.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(objectState);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
