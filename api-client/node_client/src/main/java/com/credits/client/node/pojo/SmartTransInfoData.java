package com.credits.client.node.pojo;

import java.io.Serializable;

public abstract class SmartTransInfoData implements Serializable {
    private static final long serialVersionUID = -8194301255165588832L;

    public boolean isSmartDeploy() {
        return this instanceof SmartDeployTransInfoData;
    }

    public boolean isSmartExecution() {
        return this instanceof SmartExecutionTransInfoData;
    }

    public boolean isSmartState() {
        return this instanceof SmartStateTransInfoData;
    }

    public boolean isTokenDeploy() {
        return this instanceof TokenDeployTransInfoData;
    }

    public boolean isTokenTransfer() {
        return this instanceof TokenTransferTransInfoData;
    }

    public SmartDeployTransInfoData getSmartDeployTransInfoData() {
        return (SmartDeployTransInfoData)this;
    }

    public SmartExecutionTransInfoData getSmartExecutionTransInfoData() {
        return (SmartExecutionTransInfoData)this;
    }

    public SmartStateTransInfoData getSmartStateTransInfoData() {
        return (SmartStateTransInfoData)this;
    }

    public TokenDeployTransInfoData getTokenDeployTransInfoData() {
        return (TokenDeployTransInfoData)this;
    }

    public TokenTransferTransInfoData getTokenTransferTransInfoData() {
        return (TokenTransferTransInfoData)this;
    }
}
