package com.credits.client.node.pojo;

import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.GeneralConverter;

import java.util.Optional;

public class TransactionFlowResultData extends ApiResponseData {
    private static final long serialVersionUID = -3897102320852926355L;
    private final String source;
    private final String target;
    private final Variant contractResult;
    private final int roundNumber;

    public TransactionFlowResultData(ApiResponseData apiResponseData, int roundNumber, byte[] source, byte[] target, Variant contractResult) {
        super(apiResponseData);
        this.contractResult = contractResult;
        this.roundNumber = roundNumber;
        this.source = GeneralConverter.encodeToBASE58(source);
        this.target = GeneralConverter.encodeToBASE58(target);
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public Optional<Variant> getContractResult() {
        return Optional.ofNullable(contractResult);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionFlowResultData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        TransactionFlowResultData that = (TransactionFlowResultData) o;

        if (roundNumber != that.roundNumber) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }
        if (target != null ? !target.equals(that.target) : that.target != null) {
            return false;
        }
        return contractResult != null ? contractResult.equals(that.contractResult) : that.contractResult == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (contractResult != null ? contractResult.hashCode() : 0);
        result = 31 * result + roundNumber;
        return result;
    }
}
