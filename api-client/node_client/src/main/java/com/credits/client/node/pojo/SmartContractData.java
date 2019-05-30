package com.credits.client.node.pojo;

import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class SmartContractData implements Serializable {

    private static final long serialVersionUID = 4111650022718657167L;

    private byte[] address;
    private byte[] deployer;
    private byte[] objectState;
    private SmartContractDeployData smartContractDeployData;
    private Integer transactionsCount;

    public boolean isGetterMethod() {
        return isGetterMethod;
    }

    public void setGetterMethod(boolean getterMethod) {
        isGetterMethod = getterMethod;
    }

    private boolean isGetterMethod;
    private String method;
    private List<Variant> params;
    private String base58Address;
    private int hashCode;

    public SmartContractData(byte[] address, byte[] deployer, SmartContractDeployData smartContractDeployData,
        byte[] objectState, Integer transactionsCount) {
        this.address = address;
        this.deployer = deployer;
        this.smartContractDeployData = smartContractDeployData;
        this.objectState = objectState;
        this.params = new ArrayList<>();
        this.method="";
        this.transactionsCount = transactionsCount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Variant> getParams() {
        return params;
    }

    public void setParams(List<Variant> params) {
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

    public String getBase58Address() {
        if(base58Address == null) {
            base58Address = GeneralConverter.encodeToBASE58(address);
        }
        return base58Address;
    }

    public Integer getTransactionsCount() {
        return transactionsCount;
    }

    public void setTransactionsCount(Integer transactionsCount) {
        this.transactionsCount = transactionsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartContractData)) {
            return false;
        }
        SmartContractData that = (SmartContractData) o;
        return isGetterMethod == that.isGetterMethod &&
            hashCode == that.hashCode &&
            Arrays.equals(address, that.address) &&
            Arrays.equals(deployer, that.deployer) &&
            Arrays.equals(objectState, that.objectState) &&
            Objects.equals(smartContractDeployData, that.smartContractDeployData) &&
            Objects.equals(method, that.method) &&
            Objects.equals(params, that.params) &&
            Objects.equals(base58Address, that.base58Address);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(smartContractDeployData, isGetterMethod, method, params, base58Address, hashCode);
        result = 31 * result + Arrays.hashCode(address);
        result = 31 * result + Arrays.hashCode(deployer);
        result = 31 * result + Arrays.hashCode(objectState);
        return result;
    }
}
