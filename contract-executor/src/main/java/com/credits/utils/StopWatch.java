package com.credits.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class StopWatch {
    private long startTime;
    private long spentTime;
    private final ThreadMXBean threadMXBean;

    public StopWatch() {
        threadMXBean = ManagementFactory.getThreadMXBean();
    }

    public StopWatch start() {
        spentTime = 0;
        startTime = threadMXBean.isCurrentThreadCpuTimeSupported() ? getUserTime() : getSystemTime();
        return this;
    }

    public long stop() {
        if (spentTime == 0) {
            spentTime = (threadMXBean.isCurrentThreadCpuTimeSupported() ? getUserTime() : getSystemTime()) - startTime;
        }
        return spentTime;
    }

    private long getSystemTime() {
        return System.nanoTime() / 1000_000;
    }

    private long getUserTime() {
        return threadMXBean.getCurrentThreadUserTime() / 1000_000;
    }

    @Override
    public String toString() {
        return "StopWatch{" +
                "startTime=" + startTime +
                ", spentTime=" + spentTime +
                '}';
    }
}
