package com.credits.client.node.thrift.call;

import com.credits.client.node.service.NodeApiService;
import com.credits.general.exception.CreditsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public abstract class ThriftCallThread<T> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftCallThread.class);
    private final com.credits.general.util.Callback<T> callback;
    NodeApiService nodeApiService;

    ThriftCallThread(NodeApiService nodeApiService, com.credits.general.util.Callback<T> callback) {
        this.callback = callback;
        this.nodeApiService = nodeApiService;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Start execute transaction");
            callback.onSuccess(call());
            LOGGER.info("End execute transaction");
        } catch (CreditsException e) {
            LOGGER.info(e.getMessage());
            callback.onError(e);
        }
    }

    protected abstract T call() throws CreditsException;

    @SuppressWarnings("EmptyMethod")
    public interface Callback<T> extends com.credits.general.util.Callback<T> {
    }
}
