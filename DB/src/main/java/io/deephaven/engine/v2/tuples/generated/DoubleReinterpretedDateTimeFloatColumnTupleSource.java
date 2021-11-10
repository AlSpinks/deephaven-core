package io.deephaven.engine.v2.tuples.generated;

import io.deephaven.datastructures.util.SmartKey;
import io.deephaven.engine.tables.utils.DateTime;
import io.deephaven.engine.tables.utils.DateTimeUtils;
import io.deephaven.engine.util.tuples.generated.DoubleLongFloatTuple;
import io.deephaven.engine.v2.sources.ColumnSource;
import io.deephaven.engine.v2.sources.WritableSource;
import io.deephaven.engine.v2.sources.chunk.Attributes;
import io.deephaven.engine.v2.sources.chunk.Chunk;
import io.deephaven.engine.v2.sources.chunk.DoubleChunk;
import io.deephaven.engine.v2.sources.chunk.FloatChunk;
import io.deephaven.engine.v2.sources.chunk.LongChunk;
import io.deephaven.engine.v2.sources.chunk.ObjectChunk;
import io.deephaven.engine.v2.sources.chunk.WritableChunk;
import io.deephaven.engine.v2.sources.chunk.WritableObjectChunk;
import io.deephaven.engine.v2.tuples.AbstractTupleSource;
import io.deephaven.engine.v2.tuples.ThreeColumnTupleSourceFactory;
import io.deephaven.engine.v2.tuples.TupleSource;
import io.deephaven.util.type.TypeUtils;
import org.jetbrains.annotations.NotNull;


/**
 * <p>{@link TupleSource} that produces key column values from {@link ColumnSource} types Double, Long, and Float.
 * <p>Generated by {@link io.deephaven.engine.v2.tuples.TupleSourceCodeGenerator}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DoubleReinterpretedDateTimeFloatColumnTupleSource extends AbstractTupleSource<DoubleLongFloatTuple> {

    /** {@link ThreeColumnTupleSourceFactory} instance to create instances of {@link DoubleReinterpretedDateTimeFloatColumnTupleSource}. **/
    public static final ThreeColumnTupleSourceFactory<DoubleLongFloatTuple, Double, Long, Float> FACTORY = new Factory();

    private final ColumnSource<Double> columnSource1;
    private final ColumnSource<Long> columnSource2;
    private final ColumnSource<Float> columnSource3;

    public DoubleReinterpretedDateTimeFloatColumnTupleSource(
            @NotNull final ColumnSource<Double> columnSource1,
            @NotNull final ColumnSource<Long> columnSource2,
            @NotNull final ColumnSource<Float> columnSource3
    ) {
        super(columnSource1, columnSource2, columnSource3);
        this.columnSource1 = columnSource1;
        this.columnSource2 = columnSource2;
        this.columnSource3 = columnSource3;
    }

    @Override
    public final DoubleLongFloatTuple createTuple(final long indexKey) {
        return new DoubleLongFloatTuple(
                columnSource1.getDouble(indexKey),
                columnSource2.getLong(indexKey),
                columnSource3.getFloat(indexKey)
        );
    }

    @Override
    public final DoubleLongFloatTuple createPreviousTuple(final long indexKey) {
        return new DoubleLongFloatTuple(
                columnSource1.getPrevDouble(indexKey),
                columnSource2.getPrevLong(indexKey),
                columnSource3.getPrevFloat(indexKey)
        );
    }

    @Override
    public final DoubleLongFloatTuple createTupleFromValues(@NotNull final Object... values) {
        return new DoubleLongFloatTuple(
                TypeUtils.unbox((Double)values[0]),
                DateTimeUtils.nanos((DateTime)values[1]),
                TypeUtils.unbox((Float)values[2])
        );
    }

    @Override
    public final DoubleLongFloatTuple createTupleFromReinterpretedValues(@NotNull final Object... values) {
        return new DoubleLongFloatTuple(
                TypeUtils.unbox((Double)values[0]),
                TypeUtils.unbox((Long)values[1]),
                TypeUtils.unbox((Float)values[2])
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <ELEMENT_TYPE> void exportElement(@NotNull final DoubleLongFloatTuple tuple, final int elementIndex, @NotNull final WritableSource<ELEMENT_TYPE> writableSource, final long destinationIndexKey) {
        if (elementIndex == 0) {
            writableSource.set(destinationIndexKey, tuple.getFirstElement());
            return;
        }
        if (elementIndex == 1) {
            writableSource.set(destinationIndexKey, (ELEMENT_TYPE) DateTimeUtils.nanosToTime(tuple.getSecondElement()));
            return;
        }
        if (elementIndex == 2) {
            writableSource.set(destinationIndexKey, tuple.getThirdElement());
            return;
        }
        throw new IndexOutOfBoundsException("Invalid element rowSet " + elementIndex + " for export");
    }

    @Override
    public final Object exportToExternalKey(@NotNull final DoubleLongFloatTuple tuple) {
        return new SmartKey(
                TypeUtils.box(tuple.getFirstElement()),
                DateTimeUtils.nanosToTime(tuple.getSecondElement()),
                TypeUtils.box(tuple.getThirdElement())
        );
    }

    @Override
    public final Object exportElement(@NotNull final DoubleLongFloatTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return DateTimeUtils.nanosToTime(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    public final Object exportElementReinterpreted(@NotNull final DoubleLongFloatTuple tuple, int elementIndex) {
        if (elementIndex == 0) {
            return TypeUtils.box(tuple.getFirstElement());
        }
        if (elementIndex == 1) {
            return TypeUtils.box(tuple.getSecondElement());
        }
        if (elementIndex == 2) {
            return TypeUtils.box(tuple.getThirdElement());
        }
        throw new IllegalArgumentException("Bad elementIndex for 3 element tuple: " + elementIndex);
    }

    @Override
    protected void convertChunks(@NotNull WritableChunk<? super Attributes.Values> destination, int chunkSize, Chunk<Attributes.Values> [] chunks) {
        WritableObjectChunk<DoubleLongFloatTuple, ? super Attributes.Values> destinationObjectChunk = destination.asWritableObjectChunk();
        DoubleChunk<Attributes.Values> chunk1 = chunks[0].asDoubleChunk();
        LongChunk<Attributes.Values> chunk2 = chunks[1].asLongChunk();
        FloatChunk<Attributes.Values> chunk3 = chunks[2].asFloatChunk();
        for (int ii = 0; ii < chunkSize; ++ii) {
            destinationObjectChunk.set(ii, new DoubleLongFloatTuple(chunk1.get(ii), chunk2.get(ii), chunk3.get(ii)));
        }
        destinationObjectChunk.setSize(chunkSize);
    }

    /** {@link ThreeColumnTupleSourceFactory} for instances of {@link DoubleReinterpretedDateTimeFloatColumnTupleSource}. **/
    private static final class Factory implements ThreeColumnTupleSourceFactory<DoubleLongFloatTuple, Double, Long, Float> {

        private Factory() {
        }

        @Override
        public TupleSource<DoubleLongFloatTuple> create(
                @NotNull final ColumnSource<Double> columnSource1,
                @NotNull final ColumnSource<Long> columnSource2,
                @NotNull final ColumnSource<Float> columnSource3
        ) {
            return new DoubleReinterpretedDateTimeFloatColumnTupleSource(
                    columnSource1,
                    columnSource2,
                    columnSource3
            );
        }
    }
}
