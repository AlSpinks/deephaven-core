package io.deephaven.engine.tuple.generated;

import gnu.trove.map.TIntObjectMap;
import io.deephaven.engine.tuple.CanonicalizableTuple;
import io.deephaven.engine.tuple.serialization.SerializationUtils;
import io.deephaven.engine.tuple.serialization.StreamingExternalizable;
import io.deephaven.util.compare.ObjectComparisons;
import io.deephaven.util.compare.ShortComparisons;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.function.UnaryOperator;

/**
 * <p>3-Tuple (triple) key class composed of Object, short, and short elements.
 * <p>Generated by io.deephaven.replicators.TupleCodeGenerator.
 */
public class ObjectShortShortTuple implements Comparable<ObjectShortShortTuple>, Externalizable, StreamingExternalizable, CanonicalizableTuple<ObjectShortShortTuple> {

    private static final long serialVersionUID = 1L;

    private Object element1;
    private short element2;
    private short element3;

    private transient int cachedHashCode;

    public ObjectShortShortTuple(
            final Object element1,
            final short element2,
            final short element3
    ) {
        initialize(
                element1,
                element2,
                element3
        );
    }

    /** Public no-arg constructor for {@link Externalizable} support only. <em>Application code should not use this!</em> **/
    public ObjectShortShortTuple() {
    }

    private void initialize(
            final Object element1,
            final short element2,
            final short element3
    ) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        cachedHashCode = ((31 +
                Objects.hashCode(element1)) * 31 +
                Short.hashCode(element2)) * 31 +
                Short.hashCode(element3);
    }

    public final Object getFirstElement() {
        return element1;
    }

    public final short getSecondElement() {
        return element2;
    }

    public final short getThirdElement() {
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
        final ObjectShortShortTuple typedOther = (ObjectShortShortTuple) other;
        // @formatter:off
        return ObjectComparisons.eq(element1, typedOther.element1) &&
               element2 == typedOther.element2 &&
               element3 == typedOther.element3;
        // @formatter:on
    }

    @Override
    public final int compareTo(@NotNull final ObjectShortShortTuple other) {
        if (this == other) {
            return 0;
        }
        int comparison;
        // @formatter:off
        return 0 != (comparison = ObjectComparisons.compare(element1, other.element1)) ? comparison :
               0 != (comparison = ShortComparisons.compare(element2, other.element2)) ? comparison :
               ShortComparisons.compare(element3, other.element3);
        // @formatter:on
    }

    @Override
    public void writeExternal(@NotNull final ObjectOutput out) throws IOException {
        out.writeObject(element1);
        out.writeShort(element2);
        out.writeShort(element3);
    }

    @Override
    public void readExternal(@NotNull final ObjectInput in) throws IOException, ClassNotFoundException {
        initialize(
                in.readObject(),
                in.readShort(),
                in.readShort()
        );
    }

    @Override
    public void writeExternalStreaming(@NotNull final ObjectOutput out, @NotNull final TIntObjectMap<SerializationUtils.Writer> cachedWriters) throws IOException {
        StreamingExternalizable.writeObjectElement(out, cachedWriters, 0, element1);
        out.writeShort(element2);
        out.writeShort(element3);
    }

    @Override
    public void readExternalStreaming(@NotNull final ObjectInput in, @NotNull final TIntObjectMap<SerializationUtils.Reader> cachedReaders) throws Exception {
        initialize(
                StreamingExternalizable.readObjectElement(in, cachedReaders, 0),
                in.readShort(),
                in.readShort()
        );
    }

    @Override
    public String toString() {
        return "ObjectShortShortTuple{" +
                element1 + ", " +
                element2 + ", " +
                element3 + '}';
    }

    @Override
    public ObjectShortShortTuple canonicalize(@NotNull final UnaryOperator<Object> canonicalizer) {
        final Object canonicalizedElement1 = canonicalizer.apply(element1);
        return canonicalizedElement1 == element1
                ? this : new ObjectShortShortTuple(canonicalizedElement1, element2, element3);
    }
}
