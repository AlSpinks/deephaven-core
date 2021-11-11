package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.IntComparisons;
import io.deephaven.util.compare.LongComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of long, long, and int elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class LongLongIntTuple implements Comparable<LongLongIntTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<LongLongIntTuple> {

    private static final long serialVersionUID = 1L;

    private long element1;
    private long element2;
    private int element3;

    private transient int cachedHashCode;

    public LongLongIntTuple(
            final long element1,
            final long element2,
            final int element3
    ) {
        initialize(
                element1,
                element2,
                element3
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public LongLongIntTuple() {
    }

    private void initialize(
            final long element1,
            final long element2,
            final int element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Long.hashCode(element1)) * 31 +
                Long.hashCode(element2)) * 31 +
                Integer.hashCode(element3);
    }

    public final long getFirstElement() {
        return element1;
    }

    public final long getSecondElement() {
        return element2;
    }

    public final int getThirdElement() {
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
        final LongLongIntTuple typedOther = (LongLongIntTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               element2 == typedOther.element2 &&
               element3 == typedOther.element3;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final LongLongIntTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = LongComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = LongComparisons.compare(element2, other.element2)) ? comparison :
               IntComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeLong(element1);
        out.writeLong(element2);
        out.writeInt(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readLong(),
                in.readLong(),
                in.readInt()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeLong(element1);
        out.writeLong(element2);
        out.writeInt(element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readLong(),
                in.readLong(),
                in.readInt()
        );
    }

    @Override
    public String toString() {
        return "LongLongIntTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public LongLongIntTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        return this;
    }
}
