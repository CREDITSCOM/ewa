package com.credits.utils;

import com.credits.client.executor.thrift.generated.apiexec.SmartContractGetResult;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.thrift.generated.ByteCodeObject;
import pojo.apiexec.GetSmartCodeResultData;
import pojo.apiexec.SmartContractGetResultData;

import java.util.stream.Collectors;

import static com.credits.general.util.GeneralPojoConverter.createApiResponseData;


public class ApiExecClientPojoConverter {

    public static GetSmartCodeResultData createGetSmartCodeResultData(SmartContractGetResult thriftStruct) {

        return new GetSmartCodeResultData(
                createApiResponseData(thriftStruct.getStatus()),
                thriftStruct.getByteCodeObjects().stream().map(ApiExecClientPojoConverter::createByteCodeObjectData).collect(Collectors.toList()),
                thriftStruct.getContractState()
        );
    }

    public static SmartContractGetResultData createSmartContractGetResultData(SmartContractGetResult thriftStruct) {
        return new SmartContractGetResultData(
            createApiResponseData(thriftStruct.getStatus()),
            thriftStruct.getByteCodeObjects().stream().map(ApiExecClientPojoConverter::createByteCodeObjectData).collect(Collectors.toList()),
            thriftStruct.getContractState(),
            thriftStruct.stateCanModify
        );
    }

    public static ByteCodeObjectData createByteCodeObjectData(ByteCodeObject thriftStruct) {
        return new ByteCodeObjectData(thriftStruct.getName(), thriftStruct.getByteCode());
    }
}
