package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.CharComparisons;
import io.deephaven.util.compare.DoubleComparisons;
import io.deephaven.util.compare.FloatComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of double, char, and float elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class DoubleCharFloatTuple implements Comparable<DoubleCharFloatTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<DoubleCharFloatTuple> {

    private static final long serialVersionUID = 1L;

    private double element1;
    private char element2;
    private float element3;

    private transient int cachedHashCode;

    public DoubleCharFloatTuple(
            final double element1,
            final char element2,
            final float element3
    ) {
        initialize(
                element1,
                element2,
                element3
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public DoubleCharFloatTuple() {
    }

    private void initialize(
            final double element1,
            final char element2,
            final float element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Double.hashCode(element1)) * 31 +
                Character.hashCode(element2)) * 31 +
                Float.hashCode(element3);
    }

    public final double getFirstElement() {
        return element1;
    }

    public final char getSecondElement() {
        return element2;
    }

    public final float getThirdElement() {
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
        final DoubleCharFloatTuple typedOther = (DoubleCharFloatTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               element2 == typedOther.element2 &&
               element3 == typedOther.element3;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final DoubleCharFloatTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = DoubleComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = CharComparisons.compare(element2, other.element2)) ? comparison :
               FloatComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeDouble(element1);
        out.writeChar(element2);
        out.writeFloat(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readDouble(),
                in.readChar(),
                in.readFloat()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeDouble(element1);
        out.writeChar(element2);
        out.writeFloat(element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readDouble(),
                in.readChar(),
                in.readFloat()
        );
    }

    @Override
    public String toString() {
        return "DoubleCharFloatTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public DoubleCharFloatTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        return this;
    }
}
