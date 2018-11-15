package com.credits.general.pojo;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Rustem Saidaliyev on 16.05.2018.
 */
public class SmartContractDeployData implements Serializable {

    private String sourceCode;
    private byte[] byteCode;
    private String hashState; //todo unused
    private short tokenStandart;

    public SmartContractDeployData(String sourceCode, byte[] byteCode,  short tokenStandart) {
        this.sourceCode = sourceCode;
        this.byteCode = byteCode;
        this.tokenStandart = tokenStandart;
        this.hashState="";
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

    public short getTokenStandart() {
        return tokenStandart;
    }

    public void setTokenStandart(short tokenStandart) {
        this.tokenStandart = tokenStandart;
    }

    //    public boolean isFavorite() {
//        return favorite;
//    }

//    public void setFavorite(boolean favorite) {
//        this.favorite = favorite;
//    }

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
        return tokenStandart == that.tokenStandart;
    }

    @Override
    public int hashCode() {
        int result = (sourceCode != null ? sourceCode.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(byteCode);
        result = 31 * result + (hashState != null ? hashState.hashCode() : 0);
        result = 31 * result + tokenStandart;
        return result;
    }
}
