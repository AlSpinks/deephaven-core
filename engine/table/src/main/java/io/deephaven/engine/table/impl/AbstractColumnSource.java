//
// Copyright (c) 2016-2024 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl;

import io.deephaven.base.string.cache.CharSequenceUtils;
import io.deephaven.base.verify.Assert;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.primitive.iterator.CloseableIterator;
import io.deephaven.engine.rowset.*;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.DataIndex;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.impl.chunkfillers.ChunkFiller;
import io.deephaven.engine.table.impl.chunkfilter.ChunkFilter;
import io.deephaven.engine.table.impl.chunkfilter.ChunkMatchFilterFactory;
import io.deephaven.engine.table.impl.sources.UnboxedLongBackedColumnSource;
import io.deephaven.engine.table.iterators.ChunkedColumnIterator;
import io.deephaven.engine.updategraph.UpdateGraph;
import io.deephaven.hash.KeyedObjectHashSet;
import io.deephaven.hash.KeyedObjectKey;
import io.deephaven.util.annotations.VisibleForTesting;
import io.deephaven.util.type.TypeUtils;
import io.deephaven.vector.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public abstract class AbstractColumnSource<T> implements
        ColumnSource<T>,
        DefaultChunkSource.WithPrev<Values> {

    /**
     * Minimum average run length in an {@link RowSequence} that should trigger {@link Chunk}-filling by key ranges
     * instead of individual keys.
     */
    public static final long USE_RANGES_AVERAGE_RUN_LENGTH = 5;

    protected final Class<T> type;
    protected final Class<?> componentType;

    protected final UpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph();

    protected volatile List<ColumnSource<?>> rowSetIndexerKey;

    protected AbstractColumnSource(@NotNull final Class<T> type) {
        this(type, null);
    }

    public AbstractColumnSource(@NotNull final Class<T> type, @Nullable final Class<?> elementType) {
        if (type == boolean.class) {
            // noinspection unchecked
            this.type = (Class<T>) Boolean.class;
        } else if (type == Boolean.class) {
            this.type = type;
        } else {
            // noinspection rawtypes
            final Class unboxedType = TypeUtils.getUnboxedType(type);
            // noinspection unchecked
            this.type = unboxedType != null ? unboxedType : type;
        }
        if (type.isArray()) {
            componentType = type.getComponentType();
        } else if (Vector.class.isAssignableFrom(type)) {
            if (ByteVector.class.isAssignableFrom(type)) {
                componentType = byte.class;
            } else if (CharVector.class.isAssignableFrom(type)) {
                componentType = char.class;
            } else if (DoubleVector.class.isAssignableFrom(type)) {
                componentType = double.class;
            } else if (FloatVector.class.isAssignableFrom(type)) {
                componentType = float.class;
            } else if (IntVector.class.isAssignableFrom(type)) {
                componentType = int.class;
            } else if (LongVector.class.isAssignableFrom(type)) {
                componentType = long.class;
            } else if (ShortVector.class.isAssignableFrom(type)) {
                componentType = short.class;
            } else {
                componentType = elementType == null ? Object.class : elementType;
            }
        } else {
            componentType = null;
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Class<?> getComponentType() {
        return componentType;
    }

    @Override
    public ColumnSource<T> getPrevSource() {
        return new PrevColumnSource<>(this);
    }

    @Override
    public List<ColumnSource<?>> getColumnSources() {
        List<ColumnSource<?>> localRowSetIndexerKey;
        if ((localRowSetIndexerKey = rowSetIndexerKey) == null) {
            synchronized (this) {
                if ((localRowSetIndexerKey = rowSetIndexerKey) == null) {
                    rowSetIndexerKey = localRowSetIndexerKey = Collections.singletonList(this);
                }
            }
        }
        return localRowSetIndexerKey;
    }

    @Override
    public WritableRowSet match(
            final boolean invertMatch,
            final boolean usePrev,
            final boolean caseInsensitive,
            @Nullable final DataIndex dataIndex,
            @NotNull final RowSet mapper,
            final Object... keys) {

        if (dataIndex != null) {
            final RowSetBuilderRandom allInMatchingGroups = RowSetFactory.builderRandom();

            if (caseInsensitive && (type == String.class)) {
                // Linear scan through the index table, adding case-insensitive matches
                final Table indexTable = dataIndex.table();

                KeyedObjectHashSet keySet = new KeyedObjectHashSet<>(new CIStringKey());
                Collections.addAll(keySet, keys);

                final RowSet rowSetToUse = usePrev ? indexTable.getRowSet().prev() : indexTable.getRowSet();
                final ColumnSource<?> keySource = usePrev
                        ? indexTable.getColumnSource(dataIndex.keyColumnNames()[0]).getPrevSource()
                        : indexTable.getColumnSource(dataIndex.keyColumnNames()[0]);
                final ColumnSource<RowSet> rowSetSource = usePrev
                        ? dataIndex.rowSetColumn().getPrevSource()
                        : dataIndex.rowSetColumn();

                try (final CloseableIterator<String> keyIt = ChunkedColumnIterator.make(keySource, rowSetToUse);
                        final CloseableIterator<RowSet> rowSetIt =
                                ChunkedColumnIterator.make(rowSetSource, rowSetToUse)) {
                    while (keyIt.hasNext()) {
                        final String key = keyIt.next();
                        final RowSet rowSet = rowSetIt.next();
                        if (keySet.containsKey(key)) {
                            allInMatchingGroups.addRowSet(rowSet);
                        }
                    }
                }
            } else {
                // Use the lookup function to get the matching RowSets intersected with the mapper
                final DataIndex.RowKeyLookup rowKeyLookup = dataIndex.rowKeyLookup();
                for (Object key : keys) {
                    final long rowKey = rowKeyLookup.apply(key, usePrev);
                    final RowSet range = usePrev
                            ? dataIndex.rowSetColumn().getPrev(rowKey)
                            : dataIndex.rowSetColumn().get(rowKey);
                    if (range != null) {
                        allInMatchingGroups.addRowSet(range);
                    }
                }
            }

            final WritableRowSet matchingValues;
            try (final RowSet matchingGroups = allInMatchingGroups.build()) {
                if (invertMatch) {
                    matchingValues = mapper.minus(matchingGroups);
                } else {
                    matchingValues = mapper.intersect(matchingGroups);
                }
            }
            return matchingValues;
        } else {
            return ChunkFilter.applyChunkFilter(mapper, this, usePrev,
                    ChunkMatchFilterFactory.getChunkFilter(type, caseInsensitive, invertMatch, keys));
        }
    }

    private static final class CIStringKey implements KeyedObjectKey<String, String> {
        @Override
        public String getKey(String s) {
            return s;
        }

        @Override
        public int hashKey(String s) {
            return (s == null) ? 0 : CharSequenceUtils.caseInsensitiveHashCode(s);
        }

        @Override
        public boolean equalKey(String s, String s2) {
            return (s == null) ? s2 == null : s.equalsIgnoreCase(s2);
        }
    }

    @Override
    public void fillChunk(@NotNull final FillContext context, @NotNull final WritableChunk<? super Values> destination,
            @NotNull final RowSequence rowSequence) {
        defaultFillChunk(context, destination, rowSequence);
    }

    @VisibleForTesting
    public final void defaultFillChunk(@SuppressWarnings("unused") @NotNull final FillContext context,
            @NotNull final WritableChunk<? super Values> destination,
            @NotNull final RowSequence rowSequence) {
        final ChunkFiller filler = ChunkFiller.forChunkType(destination.getChunkType());
        if (rowSequence.getAverageRunLengthEstimate() >= USE_RANGES_AVERAGE_RUN_LENGTH) {
            filler.fillByRanges(this, rowSequence, destination);
        } else {
            filler.fillByIndices(this, rowSequence, destination);
        }
    }

    @Override
    public void fillPrevChunk(@NotNull final FillContext context,
            @NotNull final WritableChunk<? super Values> destination, @NotNull final RowSequence rowSequence) {
        defaultFillPrevChunk(context, destination, rowSequence);
    }

    protected final void defaultFillPrevChunk(@SuppressWarnings("unused") @NotNull final FillContext context,
            @NotNull final WritableChunk<? super Values> destination,
            @NotNull final RowSequence rowSequence) {
        final ChunkFiller filler = ChunkFiller.forChunkType(destination.getChunkType());
        if (rowSequence.getAverageRunLengthEstimate() >= USE_RANGES_AVERAGE_RUN_LENGTH) {
            filler.fillPrevByRanges(this, rowSequence, destination);
        } else {
            filler.fillPrevByIndices(this, rowSequence, destination);
        }
    }

    @Override
    public <ALTERNATE_DATA_TYPE> boolean allowsReinterpret(
            @NotNull final Class<ALTERNATE_DATA_TYPE> alternateDataType) {
        return false;
    }

    @Override
    public final <ALTERNATE_DATA_TYPE> ColumnSource<ALTERNATE_DATA_TYPE> reinterpret(
            @NotNull final Class<ALTERNATE_DATA_TYPE> alternateDataType) throws IllegalArgumentException {
        if (!allowsReinterpret(alternateDataType)) {
            throw new IllegalArgumentException("Unsupported reinterpret for " + getClass().getSimpleName()
                    + ": type=" + getType()
                    + ", alternateDataType=" + alternateDataType);
        }
        return doReinterpret(alternateDataType);
    }

    /**
     * Supply allowed reinterpret results. The default implementation handles the most common case to avoid code
     * duplication.
     *
     * @param alternateDataType The alternate data type
     * @return The resulting {@link ColumnSource}
     */
    protected <ALTERNATE_DATA_TYPE> ColumnSource<ALTERNATE_DATA_TYPE> doReinterpret(
            @NotNull final Class<ALTERNATE_DATA_TYPE> alternateDataType) {
        if (getType() == Instant.class || getType() == ZonedDateTime.class) {
            Assert.eq(alternateDataType, "alternateDataType", long.class);
            // noinspection unchecked
            return (ColumnSource<ALTERNATE_DATA_TYPE>) new UnboxedLongBackedColumnSource<>(this);
        }
        throw new IllegalArgumentException("Unsupported reinterpret for " + getClass().getSimpleName()
                + ": type=" + getType()
                + ", alternateDataType=" + alternateDataType);
    }

    public static abstract class DefaultedMutable<DATA_TYPE> extends AbstractColumnSource<DATA_TYPE>
            implements MutableColumnSourceGetDefaults.ForObject<DATA_TYPE> {

        protected DefaultedMutable(@NotNull final Class<DATA_TYPE> type) {
            super(type);
        }

        protected DefaultedMutable(@NotNull final Class<DATA_TYPE> type, @Nullable final Class<?> elementType) {
            super(type, elementType);
        }
    }

    public static abstract class DefaultedImmutable<DATA_TYPE> extends AbstractColumnSource<DATA_TYPE>
            implements ImmutableColumnSourceGetDefaults.ForObject<DATA_TYPE> {

        protected DefaultedImmutable(@NotNull final Class<DATA_TYPE> type) {
            super(type);
        }

        protected DefaultedImmutable(@NotNull final Class<DATA_TYPE> type, @Nullable final Class<?> elementType) {
            super(type, elementType);
        }
    }
}
