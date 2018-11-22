package com.credits.client.node.util;

import com.credits.client.node.exception.NodeClientException;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.util.Converter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeClientUtilsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeClientUtils.class);

    @Test
    public void serializeByThriftTest01() throws NodeClientException {
        SmartContractDeployData smartContractDeployData = new SmartContractDeployData("sourceCode", null, (short)0);
        SmartContractData smartContractData = new SmartContractData(
            "address".getBytes(),
            "deployer".getBytes(),
            smartContractDeployData,
            null
        );

        byte[] smartContractBytes = NodeClientUtils.serializeByThrift(smartContractData);
        LOGGER.info(Converter.encodeToBASE58(smartContractBytes));
    }
}
