package com.credits.client.node.util;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractInvocationData;
import com.credits.general.pojo.ApiResponseCode;
import com.credits.general.thrift.generated.APIResponse;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.credits.general.pojo.ApiResponseCode.NOT_FOUND;
import static com.credits.general.pojo.ApiResponseCode.SUCCESS;

/**
 * Created by Rustem Saidaliyev on 09.05.2018.
 */
public class NodeClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeClientUtils.class.getName());

    public static void logApiResponse(APIResponse apiResponse) {
        byte resultCode = apiResponse.getCode();
        String resultMessage = apiResponse.getMessage();
        LOGGER.info(String.format("<--- resultCode = %s; resultMessage = %s", resultCode, resultMessage));
    }

    public static void processApiResponse(APIResponse apiResponse) throws NodeClientException {
        ApiResponseCode resultCode = ApiResponseCode.valueOf((int)apiResponse.getCode());
        if (resultCode != SUCCESS && resultCode != NOT_FOUND) {
            String resultMessage = apiResponse.getMessage();
            throw new NodeClientException(String.format("Credits Node error: %s", resultMessage));
        }
    }

    public static byte[] serializeByThrift(Object object) throws NodeClientException {
        TBase tBase;
        if (object == null) {
            throw new NodeClientException("object is null");
        }
        if (object.getClass().equals(SmartContractData.class)) {
            tBase = NodePojoConverter.smartContractDataToSmartContract((SmartContractData)object);
        } else if (object.getClass().equals(SmartContractInvocationData.class)) {
            tBase = NodePojoConverter.createSmartContractInvocation((SmartContractInvocationData)object);
        } else {
            throw new NodeClientException("Invalid TBase object");
        }
        TSerializer tSerializer = new TSerializer();
        try {
            return tSerializer.serialize(tBase);
        } catch (TException e) {
            throw new NodeClientException(e);
        }
    }
}
