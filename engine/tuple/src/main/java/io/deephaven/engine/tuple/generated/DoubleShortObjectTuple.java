package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.DoubleComparisons;
import io.deephaven.util.compare.ObjectComparisons;
import io.deephaven.util.compare.ShortComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of double, short, and Object elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class DoubleShortObjectTuple implements Comparable<DoubleShortObjectTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<DoubleShortObjectTuple> {

    private static final long serialVersionUID = 1L;

    private double element1;
    private short element2;
    private Object element3;

    private transient int cachedHashCode;

    public DoubleShortObjectTuple(
            final double element1,
            final short element2,
            final Object element3
    ) {
        initialize(
                element1,
                element2,
                element3
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public DoubleShortObjectTuple() {
    }

    private void initialize(
            final double element1,
            final short element2,
            final Object element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Double.hashCode(element1)) * 31 +
                Short.hashCode(element2)) * 31 +
                Objects.hashCode(element3);
    }

    public final double getFirstElement() {
        return element1;
    }

    public final short getSecondElement() {
        return element2;
    }

    public final Object getThirdElement() {
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
        final DoubleShortObjectTuple typedOther = (DoubleShortObjectTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               element2 == typedOther.element2 &&
               ObjectComparisons.eq(element3, typedOther.element3);
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final DoubleShortObjectTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = DoubleComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = ShortComparisons.compare(element2, other.element2)) ? comparison :
               ObjectComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeDouble(element1);
        out.writeShort(element2);
        out.writeObject(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readDouble(),
                in.readShort(),
                in.readObject()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeDouble(element1);
        out.writeShort(element2);
        StreamingExternalizable.writeObjectElement(out, cachedWriters, 2, element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readDouble(),
                in.readShort(),
                StreamingExternalizable.readObjectElement(in, cachedReaders, 2)
        );
    }

    @Override
    public String toString() {
        return "DoubleShortObjectTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public DoubleShortObjectTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        final Object canonicalizedElement3 = canonicalizer.apply(element3);
        return canonicalizedElement3 == element3
                ? this : new DoubleShortObjectTuple(element1, element2, canonicalizedElement3);
    }
}
