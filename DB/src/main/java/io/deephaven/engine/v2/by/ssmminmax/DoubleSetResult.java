/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharSetResult and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.engine.v2.by.ssmminmax;

import io.deephaven.engine.v2.sources.ArrayBackedColumnSource;
import io.deephaven.engine.v2.sources.DoubleArraySource;
import io.deephaven.engine.v2.ssms.DoubleSegmentedSortedMultiset;
import io.deephaven.engine.v2.ssms.SegmentedSortedMultiSet;

import static io.deephaven.util.QueryConstants.NULL_DOUBLE;

public class DoubleSetResult implements SsmChunkedMinMaxOperator.SetResult {
    private final boolean minimum;
    private final DoubleArraySource resultColumn;

    public DoubleSetResult(boolean minimum, ArrayBackedColumnSource resultColumn) {
        this.minimum = minimum;
        this.resultColumn = (DoubleArraySource) resultColumn;
    }

    @Override
    public boolean setResult(SegmentedSortedMultiSet ssm, long destination) {
        final double newResult;
        if (ssm.size() == 0) {
            newResult = NULL_DOUBLE;
        } else {
            final DoubleSegmentedSortedMultiset doubleSsm = (DoubleSegmentedSortedMultiset) ssm;
            newResult = minimum ? doubleSsm.getMinDouble() : doubleSsm.getMaxDouble();
        }
        return setResult(destination, newResult);
    }

    @Override
    public boolean setResultNull(long destination) {
        return setResult(destination, NULL_DOUBLE);
    }

    private boolean setResult(long destination, double newResult) {
        final double oldResult = resultColumn.getAndSetUnsafe(destination, newResult);
        return oldResult != newResult;
    }
}
