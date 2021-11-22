/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharRollupUniqueOperator and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.table.impl.by.ssmcountdistinct.unique;

import io.deephaven.engine.rowset.WritableRowSet;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.rowset.RowSetFactory;
import io.deephaven.engine.table.TableUpdate;
import io.deephaven.engine.updategraph.UpdateCommitter;
import io.deephaven.engine.table.impl.by.AggregationFactory;
import io.deephaven.engine.table.impl.by.IterativeChunkedAggregationOperator;
import io.deephaven.engine.table.impl.by.ssmcountdistinct.BucketSsmDistinctRollupContext;
import io.deephaven.engine.table.impl.by.ssmcountdistinct.ShortSsmBackedSource;
import io.deephaven.engine.table.impl.by.ssmcountdistinct.DistinctOperatorFactory;
import io.deephaven.engine.table.impl.by.ssmcountdistinct.SsmDistinctRollupContext;
import io.deephaven.engine.table.impl.sources.ShortArraySource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.chunk.Attributes.ChunkLengths;
import io.deephaven.engine.chunk.Attributes.ChunkPositions;
import io.deephaven.engine.chunk.Attributes.RowKeys;
import io.deephaven.engine.chunk.Attributes.Values;
import io.deephaven.engine.chunk.*;
import io.deephaven.engine.table.impl.ssms.ShortSegmentedSortedMultiset;
import io.deephaven.engine.table.impl.ssms.SegmentedSortedMultiSet;
import io.deephaven.engine.table.impl.util.compact.ShortCompactKernel;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This operator computes the single unique value of a particular aggregated state.  If there are no values at all
 * the 'no value key' is used.  If there are more than one values for the state,  the 'non unique key' is used.
 *
 * it is intended to be used at the second, and higher levels of rollup.
 */
public class ShortRollupUniqueOperator implements IterativeChunkedAggregationOperator {
    private final String name;

    private final ShortSsmBackedSource ssms;
    private final ShortArraySource internalResult;
    private final ColumnSource<?> externalResult;
    private final Supplier<SegmentedSortedMultiSet.RemoveContext> removeContextFactory;
    private final boolean countNull;
    private final short noValueKey;
    private final short nonUniqueKey;

    private UpdateCommitter<ShortRollupUniqueOperator> prevFlusher = null;
    private WritableRowSet touchedStates;

    public ShortRollupUniqueOperator(
                                    // region Constructor
                                    // endregion Constructor
                                    String name,
                                    boolean countNulls,
                                    short noValueKey,
                                    short nonUniqueKey) {
        this.name = name;
        this.countNull = countNulls;
        this.nonUniqueKey = nonUniqueKey;
        this.noValueKey = noValueKey;
        // region SsmCreation
        this.ssms = new ShortSsmBackedSource();
        // endregion SsmCreation
        // region ResultCreation
        this.internalResult = new ShortArraySource();
        // endregion ResultCreation
        // region ResultAssignment
        this.externalResult = internalResult;
        // endregion ResultAssignment
        removeContextFactory = SegmentedSortedMultiSet.makeRemoveContextFactory(DistinctOperatorFactory.NODE_SIZE);
    }

    //region Bucketed Updates
    private BucketSsmDistinctRollupContext updateAddValues(BucketSsmDistinctRollupContext bucketedContext,
                                                           Chunk<? extends Values> inputs,
                                                           IntChunk<ChunkPositions> starts,
                                                           IntChunk<ChunkLengths> lengths) {
        final ObjectChunk<ShortSegmentedSortedMultiset, ? extends Values> inputValues = inputs.asObjectChunk();

        bucketedContext.lengthCopy.setSize(lengths.size());
        bucketedContext.starts.setSize(lengths.size());
        if(bucketedContext.valueCopy.get() != null) {
            bucketedContext.valueCopy.get().setSize(0);
            bucketedContext.counts.get().setSize(0);
        }

        // Now fill the valueCopy set with the expanded underlying SSMs
        int currentPos = 0;
        for(int ii = 0; ii< starts.size(); ii++) {
            bucketedContext.starts.set(ii, currentPos);

            final int startPos = starts.get(ii);
            final int curLength = lengths.get(ii);
            int newLength = 0;
            for(int kk = startPos; kk < startPos + curLength; kk++) {
                final ShortSegmentedSortedMultiset ssm = inputValues.get(kk);
                final int size;
                if(ssm == null || (size = ssm.intSize()) == 0) {
                    continue;
                }

                bucketedContext.valueCopy.ensureCapacityPreserve(currentPos + newLength + size);
                ssm.fillKeyChunk(bucketedContext.valueCopy.get(), currentPos + newLength);

                newLength += size;
                //we have to do this every time otherwise ensureCapacityPreserve will not do anything.
                bucketedContext.valueCopy.get().setSize(currentPos + newLength);
            }

            // If we wrote anything into values, compact and count them, and recompute the updated length
            if(newLength > 0) {
                bucketedContext.counts.ensureCapacityPreserve(currentPos + newLength);
                bucketedContext.counts.get().setSize(currentPos + newLength);
                newLength = ShortCompactKernel.compactAndCount(bucketedContext.valueCopy.get().asWritableShortChunk(), bucketedContext.counts.get(), currentPos, newLength, countNull);
            }

            bucketedContext.lengthCopy.set(ii, newLength);
            currentPos += newLength;
        }

        return bucketedContext;
    }

    @Override
    public void addChunk(BucketedContext bucketedContext, Chunk<? extends Values> values, LongChunk<? extends RowKeys> inputIndices, IntChunk<RowKeys> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        final BucketSsmDistinctRollupContext context = updateAddValues((BucketSsmDistinctRollupContext)bucketedContext, values, startPositions, length);

        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int runLength = context.lengthCopy.get(ii);
            if (runLength == 0) {
                continue;
            }

            final int startPosition = context.starts.get(ii);
            final int origStartPos = startPositions.get(ii);
            final long destination = destinations.get(origStartPos);

            final ShortSegmentedSortedMultiset ssm = ssmForSlot(destination);
            final WritableChunk<? extends Values> valueSlice = context.valueResettable.resetFromChunk(context.valueCopy.get(), startPosition, runLength);
            final WritableIntChunk<ChunkLengths> countSlice = context.countResettable.resetFromChunk(context.counts.get(), startPosition, runLength);
            final boolean anyAdded = ssm.insert(valueSlice, countSlice);
            updateResult(ssm, destination);
            stateModified.set(ii, anyAdded);
        }
    }

    private BucketSsmDistinctRollupContext updateRemoveValues(BucketSsmDistinctRollupContext context,
                                                              Chunk<? extends Values> inputs,
                                                              IntChunk<ChunkPositions> starts,
                                                              IntChunk<ChunkLengths> lengths) {
        final ObjectChunk<ShortSegmentedSortedMultiset, ? extends Values> inputValues = inputs.asObjectChunk();

        context.lengthCopy.setSize(lengths.size());
        context.starts.setSize(lengths.size());
        if(context.valueCopy.get() != null) {
            context.valueCopy.get().setSize(0);
            context.counts.get().setSize(0);
        }

        // Now fill the valueCopy set with the expanded underlying SSMs
        int currentPos = 0;
        for(int ii = 0; ii< starts.size(); ii++) {
            context.starts.set(ii, currentPos);

            final int startPos = starts.get(ii);
            final int curLength = lengths.get(ii);
            int newLength = 0;
            for(int kk = startPos; kk < startPos + curLength; kk++) {
                final ShortSegmentedSortedMultiset ssm = inputValues.get(kk);
                final int size;
                if(ssm == null || (size = ssm.getRemovedSize()) == 0) {
                    continue;
                }

                context.valueCopy.ensureCapacityPreserve(currentPos + newLength + size);
                ssm.fillRemovedChunk(context.valueCopy.get().asWritableShortChunk(), currentPos + newLength);

                newLength += size;
                //we have to do this every time otherwise ensureCapacityPreserve will not do anything.
                context.valueCopy.get().setSize(currentPos + newLength);
            }

            // If we wrote anything into values, compact and count them, and recompute the updated length
            if(newLength > 0) {
                context.counts.ensureCapacityPreserve(currentPos + newLength);
                context.counts.get().setSize(currentPos + newLength);
                newLength = ShortCompactKernel.compactAndCount(context.valueCopy.get().asWritableShortChunk(), context.counts.get(), currentPos, newLength, countNull);
            }

            context.lengthCopy.set(ii, newLength);
            currentPos += newLength;
        }

        return context;
    }

    @Override
    public void removeChunk(BucketedContext bucketedContext, Chunk<? extends Values> values, LongChunk<? extends RowKeys> inputIndices, IntChunk<RowKeys> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        final BucketSsmDistinctRollupContext context = updateRemoveValues((BucketSsmDistinctRollupContext)bucketedContext, values, startPositions, length);

        final SegmentedSortedMultiSet.RemoveContext removeContext = removeContextFactory.get();
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int runLength = context.lengthCopy.get(ii);
            if (runLength == 0) {
                continue;
            }

            final int startPosition = context.starts.get(ii);
            final int origStartPos = startPositions.get(ii);
            final long destination = destinations.get(origStartPos);

            final ShortSegmentedSortedMultiset ssm = ssmForSlot(destination);
            final WritableChunk<? extends Values> valueSlice = context.valueResettable.resetFromChunk(context.valueCopy.get(), startPosition, runLength);
            final WritableIntChunk<ChunkLengths> countSlice = context.countResettable.resetFromChunk(context.counts.get(), startPosition, runLength);
            ssm.remove(removeContext, valueSlice, countSlice);
            if (ssm.size() == 0) {
                clearSsm(destination);
            }

            updateResult(ssm, destination);
            stateModified.set(ii, ssm.getRemovedSize() > 0);
        }
    }

    private void updateModifyAddValues(BucketSsmDistinctRollupContext context,
                                       Chunk<? extends Values> inputs,
                                       IntChunk<ChunkPositions> starts,
                                       IntChunk<ChunkLengths> lengths) {
        final ObjectChunk<ShortSegmentedSortedMultiset, ? extends Values> inputValues = inputs.asObjectChunk();

        context.lengthCopy.setSize(lengths.size());
        context.starts.setSize(lengths.size());
        if(context.valueCopy.get() != null) {
            context.valueCopy.get().setSize(0);
            context.counts.get().setSize(0);
        }

        // Now fill the valueCopy set with the expanded underlying SSMs
        int currentPos = 0;
        for(int ii = 0; ii< starts.size(); ii++) {
            context.starts.set(ii, currentPos);

            final int startPos = starts.get(ii);
            final int curLength = lengths.get(ii);
            int newLength = 0;
            for(int kk = startPos; kk < startPos + curLength; kk++) {
                final ShortSegmentedSortedMultiset ssm = inputValues.get(kk);
                final int size;
                if(ssm == null || (size = ssm.getAddedSize()) == 0) {
                    continue;
                }

                context.valueCopy.ensureCapacityPreserve(currentPos + newLength + size);
                ssm.fillAddedChunk(context.valueCopy.get().asWritableShortChunk(), currentPos + newLength);

                newLength += size;
                //we have to do this every time otherwise ensureCapacityPreserve will not do anything.
                context.valueCopy.get().setSize(currentPos + newLength);
            }

            // If we wrote anything into values, compact and count them, and recompute the updated length
            if(newLength > 0) {
                context.counts.ensureCapacityPreserve(currentPos + newLength);
                context.counts.get().setSize(currentPos + newLength);
                newLength = ShortCompactKernel.compactAndCount(context.valueCopy.get().asWritableShortChunk(), context.counts.get(), currentPos, newLength, countNull);
            }

            context.lengthCopy.set(ii, newLength);
            currentPos += newLength;
        }
    }

    @Override
    public void modifyChunk(BucketedContext bucketedContext, Chunk<? extends Values> preValues, Chunk<? extends Values> postValues, LongChunk<? extends RowKeys> postShiftIndices, IntChunk<RowKeys> destinations, IntChunk<ChunkPositions> startPositions, IntChunk<ChunkLengths> length, WritableBooleanChunk<Values> stateModified) {
        final BucketSsmDistinctRollupContext context = updateRemoveValues((BucketSsmDistinctRollupContext)bucketedContext, preValues, startPositions, length);

        final SegmentedSortedMultiSet.RemoveContext removeContext = removeContextFactory.get();
        context.ssmsToMaybeClear.fillWithValue(0, destinations.size(), false);
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int runLength = context.lengthCopy.get(ii);
            if (runLength == 0) {
                continue;
            }
            final int startPosition = context.starts.get(ii);
            final int origStartPosition = startPositions.get(ii);
            final long destination = destinations.get(origStartPosition);

            final ShortSegmentedSortedMultiset ssm = ssmForSlot(destination);
            final WritableChunk<? extends Values> valueSlice = context.valueResettable.resetFromChunk(context.valueCopy.get(), startPosition, runLength);
            final WritableIntChunk<ChunkLengths> countSlice = context.countResettable.resetFromChunk(context.counts.get(), startPosition, runLength);
            ssm.remove(removeContext, valueSlice, countSlice);
            if (ssm.size() == 0) {
                context.ssmsToMaybeClear.set(ii, true);
            }
        }

        updateModifyAddValues(context, postValues, startPositions, length);
        for (int ii = 0; ii < startPositions.size(); ++ii) {
            final int runLength = context.lengthCopy.get(ii);
            final int startPosition = context.starts.get(ii);
            final int origStartPosition = startPositions.get(ii);
            final long destination = destinations.get(origStartPosition);
            final ShortSegmentedSortedMultiset ssm = ssmForSlot(destination);

            if (runLength == 0) {
                if (context.ssmsToMaybeClear.get(ii)) {
                    // we may have deleted this position on the last round, really get rid of it
                    clearSsm(destination);
                }

                updateResult(ssm, destination);
                stateModified.set(ii, ssm.getRemovedSize() > 0);
                continue;
            }

            final WritableChunk<? extends Values> valueSlice = context.valueResettable.resetFromChunk(context.valueCopy.get(), startPosition, runLength);
            final WritableIntChunk<ChunkLengths> countSlice = context.countResettable.resetFromChunk(context.counts.get(), startPosition, runLength);
            ssm.insert(valueSlice, countSlice);
            updateResult(ssm, destination);
            stateModified.set(ii, ssm.getAddedSize() > 0 || ssm.getRemovedSize() > 0);
        }
    }
    //endregion

    //region Singleton Updates
    private SsmDistinctRollupContext updateAddValues(SsmDistinctRollupContext context,
                                                     Chunk<? extends Values> inputs) {
        final ObjectChunk<ShortSegmentedSortedMultiset, ? extends Values> values = inputs.asObjectChunk();

        if(context.valueCopy.get() != null) {
            context.valueCopy.get().setSize(0);
            context.counts.get().setSize(0);
        }

        if(values.size() == 0) {
            return context;
        }

        int currentPos = 0;
        for(int ii = 0; ii < values.size(); ii++) {
            final ShortSegmentedSortedMultiset ssm = values.get(ii);
            final int size;
            if(ssm == null || (size = ssm.intSize()) == 0) {
                continue;
            }
            context.valueCopy.ensureCapacityPreserve(currentPos + size);
            ssm.fillKeyChunk(context.valueCopy.get(), currentPos);
            currentPos += size;
            //we have to do this every time otherwise ensureCapacityPreserve will not do anything.
            context.valueCopy.get().setSize(currentPos);
        }

        if(currentPos > 0) {
            context.counts.ensureCapacityPreserve(currentPos);
            context.counts.get().setSize(currentPos);
            ShortCompactKernel.compactAndCount(context.valueCopy.get().asWritableShortChunk(), context.counts.get(), countNull);
        }
        return context;
    }

    @Override
    public boolean addChunk(SingletonContext singletonContext, int chunkSize, Chunk<? extends Values> values, LongChunk<? extends RowKeys> inputIndices, long destination) {
        final SsmDistinctRollupContext context = updateAddValues((SsmDistinctRollupContext)singletonContext, values);
        final WritableChunk<? extends Values> updatedValues = context.valueCopy.get();
        if (updatedValues == null || updatedValues.size() == 0) {
            return false;
        }

        final ShortSegmentedSortedMultiset ssm = ssmForSlot(destination);
        final boolean anyAdded =ssm.insert(updatedValues, context.counts.get());
        updateResult(ssm, destination);

        return anyAdded;
    }

    private SsmDistinctRollupContext updateRemoveValues(SsmDistinctRollupContext context,
                                                        Chunk<? extends Values> inputs) {
        final ObjectChunk<ShortSegmentedSortedMultiset, ? extends Values> values = inputs.asObjectChunk();

        if(context.valueCopy.get() != null) {
            context.valueCopy.get().setSize(0);
            context.counts.get().setSize(0);
        }
        if(values.size() == 0) {
            return context;
        }

        int currentPos = 0;
        for(int ii = 0; ii < values.size(); ii++) {
            final ShortSegmentedSortedMultiset ssm = values.get(ii);
            final int size;
            if(ssm == null || (size = ssm.getRemovedSize()) == 0) {
                continue;
            }

            context.valueCopy.ensureCapacityPreserve(currentPos + size);
            ssm.fillRemovedChunk(context.valueCopy.get().asWritableShortChunk(), currentPos);
            currentPos += size;
            //we have to do this every time otherwise ensureCapacityPreserve will not do anything.
            context.valueCopy.get().setSize(currentPos);
        }

        if(currentPos > 0) {
            context.counts.ensureCapacityPreserve(currentPos);
            context.counts.get().setSize(currentPos);
            ShortCompactKernel.compactAndCount(context.valueCopy.get().asWritableShortChunk(), context.counts.get(), countNull);
        }
        return context;
    }

    @Override
    public boolean removeChunk(SingletonContext singletonContext, int chunkSize, Chunk<? extends Values> values, LongChunk<? extends RowKeys> inputIndices, long destination) {
        final SsmDistinctRollupContext context = updateRemoveValues((SsmDistinctRollupContext)singletonContext, values);
        final WritableChunk<? extends Values> updatedValues = context.valueCopy.get();
        if (updatedValues == null || updatedValues.size() == 0) {
            return false;
        }

        final ShortSegmentedSortedMultiset ssm = ssmForSlot(destination);
        ssm.remove(context.removeContext, updatedValues, context.counts.get());
        if (ssm.size() == 0) {
            clearSsm(destination);
        }

        updateResult(ssm, destination);
        return ssm.getRemovedSize() > 0;
    }

    private void updateModifyAddValues(SsmDistinctRollupContext context,
                                       Chunk<? extends Values> inputs) {
        final ObjectChunk<ShortSegmentedSortedMultiset, ? extends Values> values = inputs.asObjectChunk();

        if(context.valueCopy.get() != null) {
            context.valueCopy.get().setSize(0);
            context.counts.get().setSize(0);
        }
        if(values.size() == 0) {
            return;
        }

        int currentPos = 0;
        for(int ii = 0; ii < values.size(); ii++) {
            final ShortSegmentedSortedMultiset ssm = values.get(ii);
            final int size;
            if(ssm == null || (size = ssm.getAddedSize()) == 0) {
                continue;
            }

            context.valueCopy.ensureCapacityPreserve(currentPos + size);
            ssm.fillAddedChunk(context.valueCopy.get().asWritableShortChunk(), currentPos);
            currentPos += size;
            //we have to do this every time otherwise ensureCapacityPreserve will not do anything.
            context.valueCopy.get().setSize(currentPos);
        }

        if(currentPos > 0) {
            context.counts.ensureCapacityPreserve(currentPos);
            context.counts.get().setSize(currentPos);
            ShortCompactKernel.compactAndCount(context.valueCopy.get().asWritableShortChunk(), context.counts.get(), countNull);
        }
    }

    @Override
    public boolean modifyChunk(SingletonContext singletonContext, int chunkSize, Chunk<? extends Values> preValues, Chunk<? extends Values> postValues, LongChunk<? extends RowKeys> postShiftIndices, long destination) {
        final SsmDistinctRollupContext context = updateRemoveValues((SsmDistinctRollupContext)singletonContext, preValues);
        ShortSegmentedSortedMultiset ssm = null;
        WritableChunk<? extends Values> updatedValues = context.valueCopy.get();
        if (updatedValues != null && updatedValues.size() > 0) {
            ssm = ssmForSlot(destination);
            ssm.remove(context.removeContext, updatedValues, context.counts.get());
        }

        updateModifyAddValues(context, postValues);
        updatedValues = context.valueCopy.get();
        if (updatedValues != null && updatedValues.size() > 0) {
            if (ssm == null) {
                ssm = ssmForSlot(destination);
            }
            ssm.insert(updatedValues, context.counts.get());
        } else if (ssm != null && ssm.size() == 0) {
            clearSsm(destination);
        } else if (ssm == null) {
            return false;
        }
        updateResult(ssm, destination);
        return ssm.getAddedSize() > 0 || ssm.getRemovedSize() > 0;
    }
    //endregion

    //region IterativeOperator / DistinctAggregationOperator
    @Override
    public void propagateUpdates(@NotNull TableUpdate downstream, @NotNull RowSet newDestinations) {
        if (touchedStates != null) {
            prevFlusher.maybeActivate();
            touchedStates.clear();
            touchedStates.insert(downstream.added());
            touchedStates.insert(downstream.modified());
        }
    }

    @Override
    public void ensureCapacity(long tableSize) {
        internalResult.ensureCapacity(tableSize);
        ssms.ensureCapacity(tableSize);
    }

    @Override
    public Map<String, ? extends ColumnSource<?>> getResultColumns() {
        final Map<String, ColumnSource<?>> columns = new LinkedHashMap<>();
        columns.put(name, externalResult);
        columns.put(name + AggregationFactory.ROLLUP_DISTINCT_SSM_COLUMN_ID + AggregationFactory.ROLLUP_COLUMN_SUFFIX, ssms.getUnderlyingSource());
        return columns;
    }

    @Override
    public void startTrackingPrevValues() {
        if(prevFlusher != null) {
            throw new IllegalStateException("startTrackingPrevValues must only be called once");
        }

        prevFlusher = new UpdateCommitter<>(this, ShortRollupUniqueOperator::flushPrevious);
        touchedStates = RowSetFactory.empty();
        ssms.startTrackingPrevValues();
        internalResult.startTrackingPrevValues();
    }

    private static void flushPrevious(ShortRollupUniqueOperator op) {
        if(op.touchedStates == null || op.touchedStates.isEmpty()) {
            return;
        }

        op.ssms.clearDeltas(op.touchedStates);
        op.touchedStates.clear();
    }
    //endregion

    //region Private Helpers
    private void updateResult(ShortSegmentedSortedMultiset ssm, long destination) {
        if(ssm.isEmpty()) {
            internalResult.set(destination, noValueKey);
        } else if(ssm.size() == 1) {
            internalResult.set(destination, ssm.get(0));
        } else {
            internalResult.set(destination, nonUniqueKey);
        }
    }

    private ShortSegmentedSortedMultiset ssmForSlot(long destination) {
        return ssms.getOrCreate(destination);
    }

    private void clearSsm(long destination) {
        ssms.clear(destination);
    }
    //endregion

    // region Contexts
    @Override
    public BucketedContext makeBucketedContext(int size) {
        return new BucketSsmDistinctRollupContext(ChunkType.Short, size);
    }

    @Override
    public SingletonContext makeSingletonContext(int size) {
        return new SsmDistinctRollupContext(ChunkType.Short);
    }

    //endregion
}
