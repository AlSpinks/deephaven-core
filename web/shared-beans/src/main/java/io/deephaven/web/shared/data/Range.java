//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.web.shared.data;

import javax.annotation.Nonnull;

/**
 * Describes a contiguous range of at least one item. Equals/hashcode compare both start and end, but comparing Range
 * instances will compare only by start - the overlap(Range) method should be used to see if two ranges share at least
 * one item.
 */
public class Range implements Comparable<Range> {
    private final long first;
    private final long last;

    public Range(long first, long last) {
        this.first = first;
        this.last = last;
    }

    public long getFirst() {
        return first;
    }

    public long getLast() {
        return last;
    }

    @Override
    public int compareTo(@Nonnull Range o) {
        return Long.compare(first, o.first);
    }

    public Range overlap(Range range) {
        if (range.first > last + 1 || range.last < first - 1) {
            // no overlap at all; note that adjacent ranges overlap/merge
            return null;
        }

        return new Range(Math.min(first, range.first), Math.max(last, range.last));
    }

    public Range[] minus(Range range) {
        if (range.first > last || range.last < first) {
            // zero overlap, return null to indicate that nothing happens at all
            return null;
        }
        if (range.first <= first && range.last >= last) {
            // entirely encompasses the current range, return nothing at all indicating that the range is just removed
            return new Range[0];
        }

        if (range.first > first && range.last < last) {
            // the subtracted section is entirely within this, slice this one into two ranges
            return new Range[] {
                    new Range(first, range.first - 1),
                    new Range(range.last + 1, last)
            };
        }
        // otherwise either the subtracted section's start is within our range _or_ its end is within our range,
        // and we can use that to only produce the one range we need to return
        if (range.first <= first) {
            return new Range[] {
                    new Range(range.last + 1, last)
            };
        } else {
            return new Range[] {
                    new Range(first, range.first - 1)
            };
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Range range = (Range) o;

        if (first != range.first)
            return false;
        return last == range.last;
    }

    @Override
    public int hashCode() {
        int result = (int) (first ^ (first >>> 32));
        result = 31 * result + (int) (last ^ (last >>> 32));
        return result;
    }

    public long size() {
        return last - first + 1;
    }

    @Override
    public String toString() {
        return "Range{" +
                "first=" + first +
                ", last=" + last +
                '}';
    }

    public Range shift(long delta) {
        return new Range(first + delta, last + delta);
    }
}
