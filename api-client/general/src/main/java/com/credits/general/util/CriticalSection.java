package com.credits.general.util;

import java.util.concurrent.locks.Lock;

public interface CriticalSection {
    static <R> R doSafe(Function<R> content, Lock lock) {
        try{
            lock.lock();
            return content.apply();
        }finally {
            lock.unlock();
        }
    }

    static void doSafe(VoidFunction content, Lock lock){
        try{
            lock.lock();
            content.apply();
        }finally {
            lock.unlock();
        }
    }

    interface VoidFunction {
        void apply();
    }
}

