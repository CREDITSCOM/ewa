package com.credits.pojo;

import com.credits.pojo.apiexec.SmartContractGetResultData;

public class ExternalSmartContract {

    public final SmartContractGetResultData contractData;
    public Object instance;

    public ExternalSmartContract(SmartContractGetResultData contractData) {
        this.contractData = contractData;
    }
}
