package com.credits.client.node.thrift.call;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.service.NodeApiService;
import com.credits.general.util.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Deprecated
public class BalanceGetThread extends ThriftCallThread<BigDecimal> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceGetThread.class);
    String address;

    public BalanceGetThread (NodeApiService nodeApiService, String address, com.credits.general.util.Callback<BigDecimal> callback) {
        super(nodeApiService, callback);
        this.address = address;
    }

    @Override
    protected BigDecimal call() throws NodeClientException, ConverterException {
        return nodeApiService.getBalance(address);
    }

}
