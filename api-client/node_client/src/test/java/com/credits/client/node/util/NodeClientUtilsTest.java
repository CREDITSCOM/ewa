package com.credits.client.node.util;

import com.credits.client.node.exception.NodeClientException;
import com.credits.general.pojo.SmartContractData;
import com.credits.general.pojo.SmartContractDeployData;
import com.credits.general.util.GeneralConverter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeClientUtilsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeClientUtils.class);

    @Test
    public void serializeByThriftTest01() throws NodeClientException {
        SmartContractDeployData smartContractDeployData = new SmartContractDeployData("sourceCode", null, TokenStandart.CreditsBasic);
        SmartContractData smartContractData = new SmartContractData(
            "address".getBytes(),
            "deployer".getBytes(),
            smartContractDeployData,
            null
        );

        byte[] smartContractBytes = NodeClientUtils.serializeByThrift(smartContractData);
        LOGGER.info(GeneralConverter.encodeToBASE58(smartContractBytes));
    }
}
