//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
// ****** AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY
// ****** Edit TupleSourceCodeGenerator and run "./gradlew replicateTupleSources" to regenerate
//
// @formatter:off
package io.deephaven.engine.table.impl.tuplesource.generated;

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
import io.deephaven.tuple.generated.ByteByteFloatTuple;
import io.deephaven.util.BooleanUtils;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Boolean, Boolean, and Float.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BooleanBooleanFloatColumnTupleSource extends AbstractTupleSource<ByteByteFloatTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link BooleanBooleanFloatColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<ByteByteFloatTuple, Boolean, Boolean, Float> FACTORY = new Factory();

    private final ColumnSource<Boolean> columnSource1;
    private final ColumnSource<Boolean> columnSource2;
    private final ColumnSource<Float> columnSource3;

    public BooleanBooleanFloatColumnTupleSource(
            @NotNull final ColumnSource<Boolean> columnSource1,
            @NotNull final ColumnSource<Boolean> columnSource2,
            @NotNull final ColumnSource<Float> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final ByteByteFloatTuple createTuple(final long rowKey) {
        return new ByteByteFloatTuple(
                BooleanUtils.booleanAsByte(columnSource1.getBoolean(rowKey)),
                BooleanUtils.booleanAsByte(columnSource2.getBoolean(rowKey)),
                columnSource3.getFloat(rowKey)
        );
    }

    @Override
    public final ByteByteFloatTuple createPreviousTuple(final long rowKey) {
        return new ByteByteFloatTuple(
                BooleanUtils.booleanAsByte(columnSource1.getPrevBoolean(rowKey)),
                BooleanUtils.booleanAsByte(columnSource2.getPrevBoolean(rowKey)),
                columnSource3.getPrevFloat(rowKey)
        );
    }

    @Override
    public final ByteByteFloatTuple createTupleFromValues(@NotNull final Object... values) {
        return new ByteByteFloatTuple(
                BooleanUtils.booleanAsByte((Boolean)values[0]),
                BooleanUtils.booleanAsByte((Boolean)values[1]),
                TypeUtils.unbox((Float)values[2])
        );
    }

    @Override
    public final ByteByteFloatTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new ByteByteFloatTuple(
                BooleanUtils.booleanAsByte((Boolean)values[0]),
                BooleanUtils.booleanAsByte((Boolean)values[1]),
                TypeUtils.unbox((Float)values[2])
        );
    }

    @Override
    public final int tupleLength() {
        return 3;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final ByteByteFloatTuple tuple, final int elementIndex, @NotNull final WritableColumnSource<ELEMENT_TYPE> writableSource, final long destinationRowKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationRowKey, (ELEMENT_TYPE) BooleanUtils.byteAsBoolean(tuple.getFirstElement()));
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationRowKey, (ELEMENT_TYPE) BooleanUtils.byteAsBoolean(tuple.getSecondElement()));
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationRowKey, tuple.getThirdElement());
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element index " + elementIndex + " for export");
    }

    @Override
    public final Object exportElement(@NotNull final ByteByteFloatTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return BooleanUtils.byteAsBoolean(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final void exportAllTo(final Object @NotNull [] dest, @NotNull final ByteByteFloatTuple tuple) {
        dest[0] = BooleanUtils.byteAsBoolean(tuple.getFirstElement());
        dest[1] = BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        dest[2] = TypeUtils.box(tuple.getThirdElement());
    }

    @Override
    public final void exportAllTo(final Object @NotNull [] dest, @NotNull final ByteByteFloatTuple tuple, final int @NotNull [] map) {
        dest[map[0]] = BooleanUtils.byteAsBoolean(tuple.getFirstElement());
        dest[map[1]] = BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        dest[map[2]] = TypeUtils.box(tuple.getThirdElement());
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final ByteByteFloatTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return BooleanUtils.byteAsBoolean(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }
    @Override
    public final void exportAllReinterpretedTo(final Object @NotNull [] dest, @NotNull final ByteByteFloatTuple tuple) {
        dest[0] = BooleanUtils.byteAsBoolean(tuple.getFirstElement());
        dest[1] = BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        dest[2] = TypeUtils.box(tuple.getThirdElement());
    }

    @Override
    public final void exportAllReinterpretedTo(final Object @NotNull [] dest, @NotNull final ByteByteFloatTuple tuple, final int @NotNull [] map) {
        dest[map[0]] = BooleanUtils.byteAsBoolean(tuple.getFirstElement());
        dest[map[1]] = BooleanUtils.byteAsBoolean(tuple.getSecondElement());
        dest[map[2]] = TypeUtils.box(tuple.getThirdElement());
    }


    @Override
    protected void convertChunks(@NotNull WritableChunk<? super Values> destination, int chunkSize, Chunk<? extends Values> [] chunks) {
        WritableObjectChunk<ByteByteFloatTuple, ? super Values> destinationObjectChunk = destination.asWritableObjectChunk();
        ObjectChunk<Boolean, ? extends Values> chunk1 = chunks[0].asObjectChunk();
        ObjectChunk<Boolean, ? extends Values> chunk2 = chunks[1].asObjectChunk();
        FloatChunk<? extends Values> chunk3 = chunks[2].asFloatChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new ByteByteFloatTuple(BooleanUtils.booleanAsByte(chunk1.get(ii)), BooleanUtils.booleanAsByte(chunk2.get(ii)), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link BooleanBooleanFloatColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<ByteByteFloatTuple, Boolean, Boolean, Float> {

        private Factory() {
        }

        @Override
        public TupleSource<ByteByteFloatTuple> create(
                @NotNull final ColumnSource<Boolean> columnSource1,
                @NotNull final ColumnSource<Boolean> columnSource2,
                @NotNull final ColumnSource<Float> columnSource3
        ) {
            return new BooleanBooleanFloatColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
