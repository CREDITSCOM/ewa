package com.credits.general.client.node.util;

import com.credits.general.client.node.pojo.SmartContractData;
import com.credits.general.client.node.pojo.SmartContractInvocationData;
import com.credits.general.client.node.exception.NodeClientException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Rustem Saidaliyev on 09.05.2018.
 */
public class ApiClientUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientUtils.class.getName());

    //TODO move to node general
//    public static void logApiResponse(APIResponse apiResponse) {
//        byte resultCode = apiResponse.getCode();
//        String resultMessage = apiResponse.getMessage();
//        LOGGER.info(String.format("<--- resultCode = %s; resultMessage = %s", resultCode, resultMessage));
//    }

    //TODO move to node general
//    public static void processApiResponse(APIResponse apiResponse) throws CreditsNodeException {
//        byte resultCode = apiResponse.getCode();
//        if (resultCode != API_RESPONSE_SUCCESS_CODE && resultCode != API_RESPONSE_NOT_FOUND) {
//            String resultMessage = apiResponse.getMessage();
//            throw new CreditsNodeException(String.format("Credits Node error: %s", resultMessage));
//        }
//    }

    public static byte[] serializeByThrift(Object object) throws NodeClientException {
        TBase tBase;
        if (object == null) {
            throw new NodeClientException("object is null");
        }
        if (object.getClass().equals(SmartContractData.class)) {
            tBase = ClientConverter.smartContractDataToSmartContract((SmartContractData)object);
        } else if (object.getClass().equals(SmartContractInvocationData.class)) {
            tBase = ClientConverter.smartContractInvocationDataToSmartContractInvocation((SmartContractInvocationData)object);
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
