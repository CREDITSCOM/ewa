package com.credits.service.contract;

import com.credits.utils.StopWatch;
import org.apache.thrift.annotation.Nullable;
import pojo.session.DeployContractSession;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static pojo.SmartContractConstants.initSmartContractConstants;

class LimitedExecutionMethod<R> {
    protected DeployContractSession session;
    private StopWatch stopWatch;
    private Throwable exception;

    LimitedExecutionMethod(DeployContractSession session) {
        this.session = session;
    }

    protected R runForLimitTime(Callable<R> block) {
        final var task = new FutureTask<>(block);
        final var limitedTimeThread = new Thread(task);
        stopWatch = new StopWatch(limitedTimeThread);
        exception = null;
        R result = null;
        try {
            limitedTimeThread.setName(session.contractAddress);
            initSmartContractConstants(limitedTimeThread.getId(), session);
            stopWatch.start();
            limitedTimeThread.start();
            result = task.get(session.executionTime, MILLISECONDS);
            stopWatch.stop();

        } catch (TimeoutException e) {
            limitedTimeThread.interrupt();

            if (limitedTimeThread.isAlive()) {
                try {
                    sleep(3);
                } catch (InterruptedException ignored) {
                }
                if (limitedTimeThread.isAlive()) {
                    limitedTimeThread.stop();
                    exception = e;
                }
            }
            stopWatch.stop();

            if (task.isDone()) {
                try {
                    result = task.get();
                } catch (Throwable ex) {
                    exception = ex;
                }
            }
        } catch (Throwable e) {
            stopWatch.stop();
            exception = e;
        }
        return result;
    }

    public long spentCpuTime() {
        return stopWatch != null ? stopWatch.getTime() : 0L;
    }

    @Nullable
    public Throwable getExceptionOrNull() {
        return exception;
    }
}
