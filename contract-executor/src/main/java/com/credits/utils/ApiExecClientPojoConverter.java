package com.credits.utils;

import com.credits.client.executor.thrift.generated.apiexec.GetSmartCodeResult;
import com.credits.general.thrift.generated.ByteCodeObject;
import com.credits.pojo.apiexec.ByteCodeObjectData;
import com.credits.pojo.apiexec.GetSmartCodeResultData;

import java.util.stream.Collectors;

import static com.credits.general.util.GeneralPojoConverter.createApiResponseData;

/**
 * Created by Rustem.Saidaliyev on 01.02.2018.
 */
public class ApiExecClientPojoConverter {

    public static GetSmartCodeResultData createGetSmartCodeResultData(GetSmartCodeResult thriftStruct) {

        return new GetSmartCodeResultData(
                createApiResponseData(thriftStruct.getStatus()),
                thriftStruct.getByteCodeObjects().stream().map(byteCodeObject -> {
                    return createByteCodeObjectData(byteCodeObject);
                }).collect(Collectors.toList()),
                thriftStruct.getContractState()
        );
    }

    public static ByteCodeObjectData createByteCodeObjectData(ByteCodeObject thriftStruct) {
        return new ByteCodeObjectData(thriftStruct.getName(), thriftStruct.getByteCode());
    }
}
