package com.credits.general.pojo;

/**
 * Created by Igor Goryunov on 17.10.2018
 */
public enum ApiResponseCode {
    SUCCESS(0),
    FAILURE(1),
    NOT_IMPLEMENTED(2),
    NOT_FOUND(3),
    UNKNOWN(-1);

    public final int code;

    ApiResponseCode(int code) {
        this.code = code;
    }

    public static ApiResponseCode valueOf(int code){
       switch (code){
           case 0: return SUCCESS;
           case 1: return FAILURE;
           case 2: return NOT_IMPLEMENTED;
           case 3: return NOT_FOUND;
           default: return UNKNOWN;
       }
    }
}
