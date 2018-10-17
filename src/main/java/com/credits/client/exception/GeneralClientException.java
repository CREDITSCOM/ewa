package com.credits.client.exception;

/**
 * Created by Igor Goryunov on 16.10.2018
 */
public class GeneralClientException  extends Throwable {
    public GeneralClientException(String errorMessage) {
        super(errorMessage);
    }

    public GeneralClientException(Exception e) {
        super(e);
    }
}
