//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
// ****** AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY
// ****** Edit TupleSourceCodeGenerator and run "./gradlew replicateTupleSources" to regenerate
//
// @formatter:off
package io.deephaven.engine.table.impl.tuplesource.generated;

import io.deephaven.chunk.ByteChunk;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.FloatChunk;
import io.deephaven.chunk.ObjectChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.WritableObjectChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.TupleSource;
import io.deephaven.engine.table.WritableColumnSource;
import io.deephaven.engine.table.impl.tuplesource.AbstractTupleSource;
import io.deephaven.engine.table.impl.tuplesource.ThreeColumnTupleSourceFactory;
import io.deephaven.tuple.generated.FloatByteObjectTuple;
import io.deephaven.util.BooleanUtils;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Float, Byte, and Object.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FloatReinterpretedBooleanObjectColumnTupleSource extends AbstractTupleSource<FloatByteObjectTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link FloatReinterpretedBooleanObjectColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<FloatByteObjectTuple, Float, Byte, Object> FACTORY = new Factory();

    private final ColumnSource<Float> columnSource1;
    private final ColumnSource<Byte> columnSource2;
    private final ColumnSource<Object> columnSource3;

    public FloatReinterpretedBooleanObjectColumnTupleSource(
            @NotNull final ColumnSource<Float> columnSource1,
            @NotNull final ColumnSource<Byte> columnSource2,
            @NotNull final ColumnSource<Object> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final FloatByteObjectTuple createTuple(final long rowKey) {
        return new FloatByteObjectTuple(
                columnSource1.getFloat(rowKey),
                columnSource2.getByte(rowKey),
                columnSource3.get(rowKey)
        );
    }

    @Override
    public final FloatByteObjectTuple createPreviousTuple(final long rowKey) {
        return new FloatByteObjectTuple(
                columnSource1.getPrevFloat(rowKey),
                columnSource2.getPrevByte(rowKey),
                columnSource3.getPrev(rowKey)
        );
    }

    @Override
    public final FloatByteObjectTuple createTupleFromValues(@NotNull final Object... values) {
        return new FloatByteObjectTuple(
                TypeUtils.unbox((Float)values[0]),
                BooleanUtils.booleanAsByte((Boolean)values[1]),
                values[2]
        );
    }

    @Override
    public final FloatByteObjectTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new FloatByteObjectTuple(
                TypeUtils.unbox((Float)values[0]),
                TypeUtils.unbox((Byte)values[1]),
                values[2]
        );
    }

    @Override
    public final int tupleLength() {
        return 3;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final FloatByteObjectTuple tuple, final int elementIndex, @NotNull final WritableColumnSource<ELEMENT_TYPE> writableSource, final long destinationRowKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationRowKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationRowKey, (ELEMENT_TYPE) BooleanUtils.byteAsBoolean(tuple.getSecondElement()));
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationRowKey, (ELEMENT_TYPE) tuple.getThirdElement());
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportElement(@NotNull final FloatByteObjectTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return tuple.getThirdElement();
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final void exportAllTo(final Object @NotNull [] dest, @NotNull final FloatByteObjectTuple tuple) {
        dest[0] = TypeUtils.box(tuple.getFirstElement());
        dest[1] = BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        dest[2] = tuple.getThirdElement();
    }

    @Override
    public final void exportAllTo(final Object @NotNull [] dest, @NotNull final FloatByteObjectTuple tuple, final int @NotNull [] map) {
        dest[map[0]] = TypeUtils.box(tuple.getFirstElement());
        dest[map[1]] = BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        dest[map[2]] = tuple.getThirdElement();
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final FloatByteObjectTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return TypeUtils.box(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return tuple.getThirdElement();
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }
    @Override
    public final void exportAllReinterpretedTo(final Object @NotNull [] dest, @NotNull final FloatByteObjectTuple tuple) {
        dest[0] = TypeUtils.box(tuple.getFirstElement());
        dest[1] = TypeUtils.box(tuple.getSecondElement());
        dest[2] = tuple.getThirdElement();
    }

    @Override
    public final void exportAllReinterpretedTo(final Object @NotNull [] dest, @NotNull final FloatByteObjectTuple tuple, final int @NotNull [] map) {
        dest[map[0]] = TypeUtils.box(tuple.getFirstElement());
        dest[map[1]] = TypeUtils.box(tuple.getSecondElement());
        dest[map[2]] = tuple.getThirdElement();
    }


    @Override
    protected void convertChunks(@NotNull WritableChunk<? super Values> destination, int chunkSize, Chunk<? extends Values> [] chunks) {
        WritableObjectChunk<FloatByteObjectTuple, ? super Values> destinationObjectChunk = destination.asWritableObjectChunk();
        FloatChunk<? extends Values> chunk1 = chunks[0].asFloatChunk();
        ByteChunk<? extends Values> chunk2 = chunks[1].asByteChunk();
        ObjectChunk<Object, ? extends Values> chunk3 = chunks[2].asObjectChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new FloatByteObjectTuple(chunk1.get(ii), chunk2.get(ii), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link FloatReinterpretedBooleanObjectColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<FloatByteObjectTuple, Float, Byte, Object> {

        private Factory() {
        }

        @Override
        public TupleSource<FloatByteObjectTuple> create(
                @NotNull final ColumnSource<Float> columnSource1,
                @NotNull final ColumnSource<Byte> columnSource2,
                @NotNull final ColumnSource<Object> columnSource3
        ) {
            return new FloatReinterpretedBooleanObjectColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
