package com.credits.general.util;

import com.credits.general.exception.CreditsException;

public interface Callback<T> {
    void onSuccess(T resultData) throws CreditsException;

    void onError(Throwable e);
}
