package com.credits.client.node.thrift.call;

import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.util.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;


public class BalanceGetThread extends ThriftCallThread<BigDecimal> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceGetThread.class);

    String address;
    NodeApiServiceImpl nodeApiService;

    public BalanceGetThread (ThriftCallThread.Callback<BigDecimal> callback, String address) {
        super(callback);
        this.address = address;
    }

    @Override
    protected BigDecimal call() throws NodeClientException, ConverterException {
        return nodeApiService.getBalance(address);
    }

}
