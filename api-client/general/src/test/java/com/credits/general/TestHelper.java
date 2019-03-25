package com.credits.general;

import java.io.Serializable;
import java.util.Objects;

public class TestHelper {

    public static class ExampleClass implements Serializable {
        String var1 = "";
        final int var2;

        public ExampleClass(int var2) {
            this.var2 = var2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ExampleClass)) {
                return false;
            }

            ExampleClass that = (ExampleClass) o;

            if (var2 != that.var2) {
                return false;
            }
            return Objects.equals(var1, that.var1);
        }

        @Override
        public int hashCode() {
            int result = var1 != null ? var1.hashCode() : 0;
            result = 31 * result + var2;
            return result;
        }
    }
}
