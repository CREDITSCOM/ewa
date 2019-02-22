package com.credits.client.executor.util;

import com.credits.client.executor.pojo.ExecuteResponseData;
import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ExecuteByteCodeResultData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.variant.VariantConverter;

import java.nio.ByteBuffer;

import static com.credits.general.util.GeneralPojoConverter.createApiResponse;
import static com.credits.general.util.GeneralPojoConverter.createApiResponseData;

public class ContractExecutorPojoConverter {

    public static ExecuteResponseData executeByteCodeResultToExecuteResponseData(ExecuteByteCodeResult result) {
        return new ExecuteResponseData(
            new ApiResponseData(ApiResponseCode.valueOf(result.getStatus().getCode()), result.getStatus().getMessage()),
            result.getRet_val());
    }

    public static ExecuteByteCodeResultData createExecuteByteCodeResultData(ExecuteByteCodeResult result) {
        return new ExecuteByteCodeResultData(createApiResponseData(result.getStatus()),
                result.contractState.array(),
                (result.getRet_val() == null ? null :VariantConverter.variantToVariantData(result.getRet_val()))
        );
    }

    public static ExecuteByteCodeResult createExecuteByteCodeResult(ExecuteByteCodeResultData data) {
        ExecuteByteCodeResult thriftSrtuct = new ExecuteByteCodeResult(
                createApiResponse(data),
                ByteBuffer.wrap(data.getContractState())
        );
        Variant retVal = (data.getRetVal() == null ? null : VariantConverter.variantDataToVariant(data.getRetVal()));
        thriftSrtuct.setRet_val(retVal);
        return thriftSrtuct;
    }

}
