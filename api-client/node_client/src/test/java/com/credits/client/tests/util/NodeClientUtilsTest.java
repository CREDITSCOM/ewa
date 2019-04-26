package com.credits.client.tests.util;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.pojo.SmartContractData;
import com.credits.client.node.pojo.SmartContractDeployData;
import com.credits.client.node.pojo.TokenStandartData;
import com.credits.client.node.util.NodeClientUtils;
import com.credits.general.util.GeneralConverter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeClientUtilsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeClientUtils.class);

    @Test
    public void serializeByThriftTest01() throws NodeClientException {
        SmartContractDeployData smartContractDeployData = new SmartContractDeployData("sourceCode", null, TokenStandartData.CreditsBasic);
        SmartContractData smartContractData = new SmartContractData(
            "address".getBytes(),
            "deployer".getBytes(),
            smartContractDeployData,
            null
        );

        byte[] smartContractBytes = NodeClientUtils.serializeByThrift(smartContractData);
        Assert.assertEquals(
                GeneralConverter.encodeToBASE58(smartContractBytes),
                "BJoykvqsEt5sSAzsgvHeo2px3SefDH7gotPequ2V6hENTJXNLt3P7jB9EXXjEdQZ6p4y2jSnGRZc7WTneeavcNynp7KsVEfHmjtLoHP3fj16K2doV"
        );
    }
}
