package com.credits.general.pojo;


import com.credits.general.thrift.generate.Variant;

/**
 * Created by Rustem Saidaliyev on 17.05.2018.
 */
public class ApiResponseData {
    private byte code;
    private String message;
    private Variant scExecRetVal;
    private String source;
    private String target;

    public ApiResponseData(
            byte code,
            String message,
            Variant scExecRetVal
    ) {
        this.code = code;
        this.message = message;
        this.scExecRetVal = scExecRetVal;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
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

