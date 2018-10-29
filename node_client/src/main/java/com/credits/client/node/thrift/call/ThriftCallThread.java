package com.credits.client.node.thrift.call;

import com.credits.general.exception.CreditsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThriftCallThread<T> implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftCallThread.class);
    private final Callback<T> callback;

    ThriftCallThread(Callback<T> callback) {
        this.callback = callback;
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
    public interface Callback<T>{
        void onSuccess(T resultData) throws CreditsException;
        void onError(Throwable e);
    }
}
