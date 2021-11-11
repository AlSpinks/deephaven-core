package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.DoubleComparisons;
import io.deephaven.util.compare.IntComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>2-Tuple (double) key class composed of double and int elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class DoubleIntTuple implements Comparable<DoubleIntTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<DoubleIntTuple> {

    private static final long serialVersionUID = 1L;

    private double element1;
    private int element2;

    private transient int cachedHashCode;

    public DoubleIntTuple(
            final double element1,
            final int element2
    ) {
        initialize(
                element1,
                element2
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public DoubleIntTuple() {
    }

    private void initialize(
            final double element1,
            final int element2
    ) {
        this.element1 = element1;
        this.element2 = element2;
        cachedHashCode = (31 +
                Double.hashCode(element1)) * 31 +
                Integer.hashCode(element2);
    }

    public final double getFirstElement() {
        return element1;
    }

    public final int getSecondElement() {
        return element2;
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
        final DoubleIntTuple typedOther = (DoubleIntTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               element2 == typedOther.element2;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final DoubleIntTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = DoubleComparisons.compare(element1, other.element1)) ? comparison :
               IntComparisons.compare(element2, other.element2);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeDouble(element1);
        out.writeInt(element2);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readDouble(),
                in.readInt()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeDouble(element1);
        out.writeInt(element2);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readDouble(),
                in.readInt()
        );
    }

    @Override
    public String toString() {
        return "DoubleIntTuple{" +
                element1 + ", " +
                element2 + '}';
    }

    @Override
    public DoubleIntTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        return this;
    }
}
