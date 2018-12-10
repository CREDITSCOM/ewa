package com.credits.general.pojo;

import com.credits.general.thrift.generated.TokenStandart;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Rustem Saidaliyev on 16.05.2018.
 */
public class SmartContractDeployData implements Serializable {

    private static final long serialVersionUID = -6187425771734674520L;
    private final String sourceCode;
    private final byte[] byteCode;
    private final String hashState; //unused
    private final TokenStandart tokenStandard;

    public SmartContractDeployData(String sourceCode, byte[] byteCode,  TokenStandart tokenStandard) {
        this.sourceCode = sourceCode;
        this.byteCode = byteCode;
        this.tokenStandard = tokenStandard;
        this.hashState="";
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public String getHashState() {
        return hashState;
    }

    public TokenStandart getTokenStandard() {
        return tokenStandard;
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
        if (!Arrays.equals(byteCode, that.byteCode)) {
            return false;
        }
        if (hashState != null ? !hashState.equals(that.hashState) : that.hashState != null) {
            return false;
        }
        return tokenStandard == that.tokenStandard;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int hashCode() {
        int result = (sourceCode != null ? sourceCode.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(byteCode);
        result = 31 * result + (hashState != null ? hashState.hashCode() : 0);
        result = 31 * result + tokenStandard.hashCode();
        return result;
    }
}
