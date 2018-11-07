package com.credits.general.pojo;


import com.credits.general.thrift.generated.Variant;

/**
 * Created by Rustem Saidaliyev on 17.05.2018.
 */
public class ApiResponseData {
    private ApiResponseCode code;
    private String message;
    private Variant scExecRetVal;
    private String source;
    private String target;

    public ApiResponseData(
            ApiResponseCode code,
            String message,
            Variant scExecRetVal
    ) {
        this.code = code;
        this.message = message;
        this.scExecRetVal = scExecRetVal;
    }

    public ApiResponseCode getCode() {
        return code;
    }

    public void setCode(ApiResponseCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Variant getScExecRetVal() {
        return scExecRetVal;
    }

    public void setScExecRetVal(Variant scExecRetVal) {
        this.scExecRetVal = scExecRetVal;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}

