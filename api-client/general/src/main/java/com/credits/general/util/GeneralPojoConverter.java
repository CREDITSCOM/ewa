package com.credits.general.util;

import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ClassObjectData;
import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.ClassObject;

import java.nio.ByteBuffer;

import static com.credits.general.util.GeneralConverter.byteCodeObjectsDataToByteCodeObjects;
import static com.credits.general.util.GeneralConverter.byteCodeObjectsToByteCodeObjectsData;

public class GeneralPojoConverter {

    public static ApiResponseData createApiResponseData(APIResponse thriftStruct) {
        return new ApiResponseData(ApiResponseCode.valueOf(thriftStruct.getCode()), thriftStruct.getMessage());
    }

    public static APIResponse createApiResponse(ApiResponseData data) {
        return new APIResponse((byte)data.getCode().code, data.getMessage());
    }

    public static ClassObjectData createClassObjectData(ClassObject thriftStruct) {
        return new ClassObjectData(byteCodeObjectsToByteCodeObjectsData(thriftStruct.byteCodeObjects), thriftStruct.instance.array());
    }

    public static ClassObject createClassObject(ClassObjectData data) {
        return new ClassObject(byteCodeObjectsDataToByteCodeObjects(data.getByteCodeObjects()), ByteBuffer.wrap(data.getInstance()));
    }
}
