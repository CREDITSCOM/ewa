package com.credits.scapi.v1;

import com.credits.general.util.Function;
import service.node.NodeApiExecInteractionService;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;

public abstract class SmartContract extends com.credits.scapi.v0.SmartContract{
    private static final long serialVersionUID = 6480522228889919741L;

    private static NodeApiExecInteractionService nodeApiService;
    private static ExecutorService cachedPool;

    public SmartContract() {
        super();
    }

    final protected BigDecimal getBalance(String addressBase58) {
        return callService(() -> nodeApiService.getBalance(addressBase58));
    }

    private <R> R callService(Function<R> method) {
        try {
            return cachedPool.submit(method::apply).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
