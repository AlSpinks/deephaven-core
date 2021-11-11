package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.ByteComparisons;
import io.deephaven.util.compare.ObjectComparisons;
import io.deephaven.util.compare.ShortComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of short, Object, and byte elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class ShortObjectByteTuple implements Comparable<ShortObjectByteTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<ShortObjectByteTuple> {

    private static final long serialVersionUID = 1L;

    private short element1;
    private Object element2;
    private byte element3;

    private transient int cachedHashCode;

    public ShortObjectByteTuple(
            final short element1,
            final Object element2,
            final byte element3
    ) {
        initialize(
                element1,
                element2,
                element3
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public ShortObjectByteTuple() {
    }

    private void initialize(
            final short element1,
            final Object element2,
            final byte element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Short.hashCode(element1)) * 31 +
                Objects.hashCode(element2)) * 31 +
                Byte.hashCode(element3);
    }

    public final short getFirstElement() {
        return element1;
    }

    public final Object getSecondElement() {
        return element2;
    }

    public final byte getThirdElement() {
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
        final ShortObjectByteTuple typedOther = (ShortObjectByteTuple) other;
        // @formatter:off
        return element1 == typedOther.element1 &&
               ObjectComparisons.eq(element2, typedOther.element2) &&
               element3 == typedOther.element3;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final ShortObjectByteTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = ShortComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = ObjectComparisons.compare(element2, other.element2)) ? comparison :
               ByteComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeShort(element1);
        out.writeObject(element2);
        out.writeByte(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readShort(),
                in.readObject(),
                in.readByte()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        out.writeShort(element1);
        StreamingExternalizable.writeObjectElement(out, cachedWriters, 1, element2);
        out.writeByte(element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                in.readShort(),
                StreamingExternalizable.readObjectElement(in, cachedReaders, 1),
                in.readByte()
        );
    }

    @Override
    public String toString() {
        return "ShortObjectByteTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public ShortObjectByteTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        final Object canonicalizedElement2 = canonicalizer.apply(element2);
        return canonicalizedElement2 == element2
                ? this : new ShortObjectByteTuple(element1, canonicalizedElement2, element3);
    }
}
