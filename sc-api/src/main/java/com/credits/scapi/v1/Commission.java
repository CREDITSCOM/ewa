package com.credits.scapi.v1;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import static com.credits.general.util.Utils.calculateActualFee;

public class Commission implements Serializable {
    private static final long serialVersionUID = -6661456629742308909L;
    private final short feeShort;
    private final double feeDouble;

    public Commission(double value) {
        final Pair<Double, Short> actualOfferedMaxFee = calculateActualFee(value);
        feeDouble = actualOfferedMaxFee.getLeft();
        feeShort = actualOfferedMaxFee.getRight();
    }

    public short getFeeShort() {
        return feeShort;
    }

    public double getFeeDouble() {
        return feeDouble;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Commission)) {
            return false;
        }
        Commission that = (Commission) o;
        return feeShort == that.feeShort;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feeShort);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Commission.class.getSimpleName() + "[", "]")
            .add("feeShort=" + feeShort)
            .add("feeDouble=" + feeDouble)
            .toString();
    }
}
