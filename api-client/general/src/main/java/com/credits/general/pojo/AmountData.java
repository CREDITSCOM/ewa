package com.credits.general.pojo;

import java.util.Objects;

public class AmountData implements Amount {

    final int integral;
    final long fraction;

    public AmountData(int integral, long fraction) {
        this.integral = integral;
        this.fraction = fraction;
    }

    public AmountData(com.credits.general.thrift.generated.Amount amount) {
        this(amount.getIntegral(), amount.getFraction());
    }

    @Override
    public int getIntegral() {
        return integral;
    }

    @Override
    public long getFractional() {
        return fraction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmountData that = (AmountData) o;
        return integral == that.integral &&
                fraction == that.fraction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integral, fraction);
    }

    @Override
    public String toString() {
        return "AmountData{" +
                "integral=" + integral +
                ", fraction=" + fraction +
                '}';
    }
}

