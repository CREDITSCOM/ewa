package com.credits.client.node.pojo;

import com.credits.general.pojo.ByteCodeObjectData;
import java.io.Serializable;
import java.util.List;


public class SmartContractDeployData implements Serializable {

    private static final long serialVersionUID = -6187425771734674520L;
    private final String sourceCode;
    private final List<ByteCodeObjectData> byteCodeObjectDataList;
    private final String hashState; //unused
    private final TokenStandartData tokenStandardData;

    public SmartContractDeployData(String sourceCode, List<ByteCodeObjectData> byteCodeObjectDataList,  TokenStandartData tokenStandardData) {
        this.sourceCode = sourceCode;
        this.byteCodeObjectDataList = byteCodeObjectDataList;
        this.tokenStandardData = tokenStandardData;
        this.hashState="";
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public List<ByteCodeObjectData> getByteCodeObjects() {
        return byteCodeObjectDataList;
    }

    public String getHashState() {
        return hashState;
    }

    public TokenStandartData getTokenStandardData() {
        return tokenStandardData;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SmartContractDeployData that = (SmartContractDeployData) o;

        if (sourceCode != null ? !sourceCode.equals(that.sourceCode) : that.sourceCode != null) {
            return false;
        }
        if (byteCodeObjectDataList.equals(that.byteCodeObjectDataList)) {
            return false;
        }
        if (hashState != null ? !hashState.equals(that.hashState) : that.hashState != null) {
            return false;
        }
        return tokenStandardData == that.tokenStandardData;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int hashCode() {
        int result = (sourceCode != null ? sourceCode.hashCode() : 0);
        result = 31 * result + byteCodeObjectDataList.hashCode();
        result = 31 * result + (hashState != null ? hashState.hashCode() : 0);
        result = 31 * result + tokenStandardData.hashCode();
        return result;
    }
}
