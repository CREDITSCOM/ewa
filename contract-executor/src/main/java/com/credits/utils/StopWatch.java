package com.credits.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class StopWatch {
    private volatile long startTime;
    private volatile long spentTime;
    private final ThreadMXBean threadMXBean;
    private final long threadId;

    public StopWatch(Thread measuredThread) {
        threadId = measuredThread.getId();
        threadMXBean = ManagementFactory.getThreadMXBean();
    }

    public StopWatch start() {
        spentTime = 0;
        startTime = threadMXBean.isThreadCpuTimeEnabled() ? getUserTime() : getSystemTime();
        return this;
    }

    public long stop() {
        if (spentTime == 0) {
            spentTime = (threadMXBean.isThreadCpuTimeEnabled() ? getUserTime() : getSystemTime()) - startTime;
        }
        return spentTime;
    }

    public long getTime(){
        return spentTime;
    }

    private long getSystemTime() {
        return System.nanoTime() ;
    }

    private long getUserTime() {
        return threadMXBean.getThreadCpuTime(threadId) ;
    }

    @Override
    public String toString() {
        return "StopWatch{" +
                "startTime=" + startTime +
                ", spentTime=" + spentTime +
                '}';
    }
}
