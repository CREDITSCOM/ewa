package com.credits.client.node.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rustem Saidaliyev on 06.08.2018.
 */
public class SmartContractInvocationData implements Serializable {

    protected static final long serialVersionUID = 4544650022718657168L;
    private String sourceCode;
    private byte[] byteCode;
    private String hashState;
    private String method;
    private List<Object> params;
    private boolean forgetNewState;

    public SmartContractInvocationData(String sourceCode, byte[] byteCode, String hashState, String method, List<Object> params, boolean forgetNewState) {
        this.sourceCode = sourceCode;
        this.byteCode = byteCode;
        this.hashState = hashState;
        this.method = method;
        this.params = params;
        this.forgetNewState = forgetNewState;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public void setByteCode(byte[] byteCode) {
        this.byteCode = byteCode;
    }

    public String getHashState() {
        return hashState;
    }

    public void setHashState(String hashState) {
        this.hashState = hashState;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public boolean isForgetNewState() {
        return forgetNewState;
    }

    public void setForgetNewState(boolean forgetNewState) {
        this.forgetNewState = forgetNewState;
    }
}
