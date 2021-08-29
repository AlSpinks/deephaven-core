/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit SumFloatChunk and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.engine.v2.by;

import io.deephaven.util.QueryConstants;
import io.deephaven.engine.v2.sources.chunk.Attributes;
import io.deephaven.engine.v2.sources.chunk.DoubleChunk;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

class SumDoubleChunk {
    private SumDoubleChunk() {} // static use only

    static double sumDoubleChunk(DoubleChunk<? extends Attributes.Values> values, int chunkStart, int chunkSize,
                                MutableInt chunkNormalCount,
                                MutableInt chunkNanCount,
                                MutableInt chunkInfinityCount,
                                MutableInt chunkMinusInfinityCount) {
        final int end = chunkStart + chunkSize;
        double sum = 0;
        for (int ii = chunkStart; ii < end; ++ii) {
            final double aDouble = values.get(ii);

            if (Double.isNaN(aDouble)) {
                chunkNanCount.increment();
            } else if (aDouble == Double.POSITIVE_INFINITY) {
                chunkInfinityCount.increment();
            } else if (aDouble == Double.NEGATIVE_INFINITY) {
                chunkMinusInfinityCount.increment();
            } else if (!(aDouble == QueryConstants.NULL_DOUBLE)) {
                sum += aDouble;
                chunkNormalCount.increment();
            }
        }
        return sum;
    }

    static double sum2DoubleChunk(DoubleChunk<? extends Attributes.Values> values, int chunkStart, int chunkSize,
                                 MutableInt chunkNormalCount,
                                 MutableInt chunkNanCount,
                                 MutableInt chunkInfinityCount,
                                 MutableInt chunkMinusInfinityCount,
                                 MutableDouble sum2out) {
        final int end = chunkStart + chunkSize;
        double sum = 0;
        double sum2 = 0;

        for (int ii = chunkStart; ii < end; ++ii) {
            final double value = values.get(ii);
            if (value != QueryConstants.NULL_DOUBLE) {
                if (Double.isNaN(value)) {
                    chunkNanCount.increment();
                } else if (value == Double.POSITIVE_INFINITY) {
                    chunkInfinityCount.increment();
                } else if (value == Double.NEGATIVE_INFINITY) {
                    chunkMinusInfinityCount.increment();
                } else {
                    sum += value;
                    sum2 += (double) value * (double) value;
                    chunkNormalCount.increment();
                }
            }
        }

        sum2out.setValue(sum2);

        return sum;
    }

    static double sumDoubleChunkAbs(DoubleChunk<? extends Attributes.Values> values, int chunkStart, int chunkSize,
                                   MutableInt chunkNormalCount,
                                   MutableInt chunkNanCount,
                                   MutableInt chunkInfinityCount) {
        final int end = chunkStart + chunkSize;
        double sum = 0;
        for (int ii = chunkStart; ii < end; ++ii) {
            final double aDouble = values.get(ii);

            if (Double.isNaN(aDouble)) {
                chunkNanCount.increment();
            } else if (aDouble == Double.POSITIVE_INFINITY || aDouble == Double.NEGATIVE_INFINITY) {
                chunkInfinityCount.increment();
            } else if (!(aDouble == QueryConstants.NULL_DOUBLE)) {
                sum += Math.abs(aDouble);
                chunkNormalCount.increment();
            }
        }
        return sum;
    }
}
