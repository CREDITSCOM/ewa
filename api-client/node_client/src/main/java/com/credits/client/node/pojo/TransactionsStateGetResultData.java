package com.credits.client.node.pojo;

import com.credits.general.pojo.ApiResponseData;

import java.util.Map;

public class TransactionsStateGetResultData  extends ApiResponseData {

    private static final long serialVersionUID = -2766052346378975296L;
    private final Map<Long, TransactionStateData> states;
    private final int roundNumber;

    public TransactionsStateGetResultData(ApiResponseData apiResponseData, Map<Long, TransactionStateData> states, int roundNumber) {
        super(apiResponseData);
        this.states = states;
        this.roundNumber = roundNumber;
    }

    public Map<Long, TransactionStateData> getStates() {
        return states;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TransactionsStateGetResultData that = (TransactionsStateGetResultData) o;

        if (roundNumber != that.roundNumber) return false;
        return states.equals(that.states);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + states.hashCode();
        result = 31 * result + roundNumber;
        return result;
    }

    @Override
    public String toString() {
        return "TransactionsStateGetResultData{" +
                "states=" + states +
                ", roundNumber=" + roundNumber +
                '}';
    }
}
