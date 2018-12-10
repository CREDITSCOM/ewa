package com.credits.general.pojo;

import com.credits.general.util.GeneralConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rustem Saidaliyev on 16.05.2018.
 */
public class SmartContractData implements Serializable {

    private static final long serialVersionUID = 4111650022718657167L;

    private byte[] address;
    private byte[] deployer;
    private byte[] objectState;
    private SmartContractDeployData smartContractDeployData;
    private String method;
    private List<Object> params;
    private String base58Address;
    private int hashCode;

    public SmartContractData(byte[] address, byte[] deployer, SmartContractDeployData smartContractDeployData,
        byte[] objectState) {
        this.address = address;
        this.deployer = deployer;
        this.smartContractDeployData = smartContractDeployData;
        this.objectState = objectState;
        this.params = new ArrayList<>();
        this.method="";
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
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

    public SmartContractDeployData getSmartContractDeployData() {
        return smartContractDeployData;
    }

    public void setSmartContractDeployData(SmartContractDeployData smartContractDeployData) {
        this.smartContractDeployData = smartContractDeployData;
    }

//    public boolean isFavorite() {
//        return favorite;
//    }

//    public void setFavorite(boolean favorite) {
//        this.favorite = favorite;
//    }

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

        // TODO add smartContractDeployData equals

        if (!Arrays.equals(objectState, that.objectState)) {
            return false;
        }
        if (method != null ? !method.equals(that.method) : that.method != null) {
            return false;
        }
        return params != null ? params.equals(that.params) : that.params == null;
    }

    public String getBase58Address() {
        if(base58Address == null) {
            base58Address = GeneralConverter.encodeToBASE58(address);
        }
        return base58Address;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(address);
        result = 31 * result + Arrays.hashCode(deployer);
        // TODO add smartContractDeployData hashCode
        result = 31 * result + Arrays.hashCode(objectState);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
