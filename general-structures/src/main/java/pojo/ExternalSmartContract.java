package pojo;

import pojo.apiexec.SmartContractGetResultData;

public class ExternalSmartContract {

    public final SmartContractGetResultData contractData;
    public Object instance;

    public ExternalSmartContract(SmartContractGetResultData contractData) {
        this.contractData = contractData;
    }
}
