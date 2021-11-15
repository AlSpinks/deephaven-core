/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2.by;

import java.util.*;

public class AggregationFormulaSpec extends AggregationGroupSpec {

    private final String formula;
    private final String columnParamName;

    public AggregationFormulaSpec(String formula, String columnParamName) {
        this.formula = formula;
        this.columnParamName = columnParamName;
    }

    public String getFormula() {
        return formula;
    }

    public String getColumnParamName() {
        return columnParamName;
    }

    private static class MemoKey implements AggregationMemoKey {
        private final String formula;
        private final String columnParamName;

        private MemoKey(String formula, String columnParamName) {
            this.formula = formula;
            this.columnParamName = columnParamName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final MemoKey memoKey = (MemoKey) o;
            return Objects.equals(formula, memoKey.formula) &&
                    Objects.equals(columnParamName, memoKey.columnParamName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(formula, columnParamName);
        }
    }

    @Override
    public AggregationMemoKey getMemoKey() {
        return new MemoKey(formula, columnParamName);
    }
}
