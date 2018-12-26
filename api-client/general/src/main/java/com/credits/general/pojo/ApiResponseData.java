package com.credits.general.pojo;


import java.io.Serializable;

/**
 * Created by Rustem Saidaliyev on 17.05.2018.
 */
public class ApiResponseData implements Serializable {
    private static final long serialVersionUID = 4647719018709155500L;
    private final ApiResponseCode code;
    private final String message;
    //    private String source;
    //    private String target;
    //    private long transactionId;

    public ApiResponseData(ApiResponseData apiResponseData){
        this(apiResponseData.getCode(), apiResponseData.getMessage());
    }

    public ApiResponseData(ApiResponseCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResponseCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiResponseData)) {
            return false;
        }

        ApiResponseData that = (ApiResponseData) o;

        if (code != that.code) {
            return false;
        }
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}


