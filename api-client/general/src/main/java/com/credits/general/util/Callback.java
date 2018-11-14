package com.credits.general.util;

import com.credits.general.exception.CompilationException;
import com.credits.general.exception.CreditsException;

public interface Callback<T> {
    void onSuccess(T resultData) throws CreditsException, CompilationException;

    void onError(Throwable e);
}
