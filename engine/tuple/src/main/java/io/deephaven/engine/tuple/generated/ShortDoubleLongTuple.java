package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.DoubleComparisons;
import io.deephaven.util.compare.LongComparisons;
import io.deephaven.util.compare.ShortComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of short, double, and long elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class ShortDoubleLongTuple implements Comparable<ShortDoubleLongTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<ShortDoubleLongTuple> {

    private static final long serialVersionUID = 1L;

    private short element1;
    private double element2;
    private long element3;

    private transient int cachedHashCode;

    public ShortDoubleLongTuple(
            final short element1,
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
    public ShortDoubleLongTuple() {
    }

    private void initialize(
            final short element1,
            final double element2,
            final long element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Short.hashCode(element1)) * 31 +
                Double.hashCode(element2)) * 31 +
                Long.hashCode(element3);
    }

    public final short getFirstElement() {
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
        final ShortDoubleLongTuple typedOther = (ShortDoubleLongTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               element2 == typedOther.element2 &&
               element3 == typedOther.element3;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final ShortDoubleLongTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = ShortComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = DoubleComparisons.compare(element2, other.element2)) ? comparison :
               LongComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeShort(element1);
        out.writeDouble(element2);
        out.writeLong(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readShort(),
                in.readDouble(),
                in.readLong()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeShort(element1);
        out.writeDouble(element2);
        out.writeLong(element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readShort(),
                in.readDouble(),
                in.readLong()
        );
    }

    @Override
    public String toString() {
        return "ShortDoubleLongTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public ShortDoubleLongTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        return this;
    }
}
