//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.plot.datasets.histogram;

import io.deephaven.function.Numeric;

import java.io.Serializable;

import static io.deephaven.util.QueryConstants.NULL_LONG;

/**
 * {@link DiscretizedRange} where all bins are equal length.
 */
public class DiscretizedRangeEqual implements DiscretizedRange, Serializable {
    private static final long serialVersionUID = 1537977750216956112L;

    private final double min;
    private final double max;
    private final double binWidth;

    /**
     * Creates a DiscretizedRangeEqual instance with specified {@code min} and {@code max} with {@code nBins} equally
     * sized bins.
     *
     * @param min minimum of the total range
     * @param max maximum of the total range
     * @param nBins number of bins
     */
    public DiscretizedRangeEqual(double min, double max, int nBins) {
        this.min = min;
        this.max = max;
        this.binWidth = (max - min) / nBins;
    }

    @Override
    public double binMin(long index) {
        return min + binWidth * index;
    }

    @Override
    public double binMax(long index) {
        return min + binWidth * (index + 1);
    }

    @Override
    public long index(double value) {
        if (!Numeric.isFinite(value) || value < min || value > max) {
            return NULL_LONG;
        }

        final long index = (long) ((value - min) / binWidth);
        return value == max ? index - 1 : index; // if value is at max, it would be placed in an extra bin
    }
}
