package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.DoubleComparisons;
import io.deephaven.util.compare.LongComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of long, double, and long elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class LongDoubleLongTuple implements Comparable<LongDoubleLongTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<LongDoubleLongTuple> {

    private static final long serialVersionUID = 1L;

    private long element1;
    private double element2;
    private long element3;

    private transient int cachedHashCode;

    public LongDoubleLongTuple(
            final long element1,
            final double element2,
            final long element3
    ) {
        initialize(
                element1,
                element2,
                element3
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public LongDoubleLongTuple() {
    }

    private void initialize(
            final long element1,
            final double element2,
            final long element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Long.hashCode(element1)) * 31 +
                Double.hashCode(element2)) * 31 +
                Long.hashCode(element3);
    }

    public final long getFirstElement() {
        return element1;
    }

    public final double getSecondElement() {
        return element2;
    }

    public final long getThirdElement() {
        return element3;
    }

    @Override
    public final int hashCode() {
        return cachedHashCode;
    }

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final LongDoubleLongTuple typedOther = (LongDoubleLongTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               element2 == typedOther.element2 &&
               element3 == typedOther.element3;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final LongDoubleLongTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = LongComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = DoubleComparisons.compare(element2, other.element2)) ? comparison :
               LongComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeLong(element1);
        out.writeDouble(element2);
        out.writeLong(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readLong(),
                in.readDouble(),
                in.readLong()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeLong(element1);
        out.writeDouble(element2);
        out.writeLong(element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readLong(),
                in.readDouble(),
                in.readLong()
        );
    }

    @Override
    public String toString() {
        return "LongDoubleLongTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public LongDoubleLongTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        return this;
    }
}
