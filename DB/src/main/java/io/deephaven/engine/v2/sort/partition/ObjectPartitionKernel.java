/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharPartitionKernel and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.engine.v2.sort.partition;

import java.util.Objects;

import io.deephaven.engine.util.tuples.generated.ObjectLongTuple;
import io.deephaven.engine.v2.sort.timsort.ObjectLongTimsortKernel;
import io.deephaven.engine.v2.sort.LongSortKernel;
import io.deephaven.engine.v2.sources.ColumnSource;
import io.deephaven.engine.structures.chunk.*;
import io.deephaven.engine.structures.chunk.Attributes.*;
import io.deephaven.engine.v2.utils.Index;

import java.util.stream.IntStream;

public class ObjectPartitionKernel {
    public static class PartitionKernelContext {
        // during the actual partition operation, we stick the new keys in here; when we exceed chunksize afterwards,
        // we can pass the entire chunk value to the builder; which then makes the virtual call to build it all at once
        private final WritableLongChunk<KeyIndices>[] accumulatedKeys;
        private final Index.SequentialBuilder [] builders;

        private final int chunkSize;
        private final WritableObjectChunk<Object, Any> pivotValues;
        private final WritableLongChunk<KeyIndices> pivotKeys;
        private final boolean preserveEquality;

        private PartitionKernelContext(int chunkSize, int numPartitions, boolean preserveEquality) {
            this.chunkSize = chunkSize;
            this.preserveEquality = preserveEquality;

            pivotValues = WritableObjectChunk.makeWritableChunk(numPartitions - 1);
            pivotKeys = WritableLongChunk.makeWritableChunk(numPartitions - 1);
            if (preserveEquality) {
                //noinspection unchecked
                accumulatedKeys = new WritableLongChunk[numPartitions * 2 - 1];
                builders = new Index.SequentialBuilder[numPartitions * 2 - 1];
            } else {
                //noinspection unchecked
                accumulatedKeys = new WritableLongChunk[numPartitions];
                builders = new Index.SequentialBuilder[numPartitions];
            }
            for (int ii = 0; ii < builders.length; ++ii) {
                builders[ii] = Index.FACTORY.getSequentialBuilder();
                accumulatedKeys[ii] = WritableLongChunk.makeWritableChunk(chunkSize);
                accumulatedKeys[ii].setSize(0);
            }
        }

        public Index [] getPartitions(boolean resetBuilders) {
            final Index [] partitions = new Index[builders.length];
            flushAllToBuilders(this);
            for (int ii = 0; ii < builders.length; ++ii) {
                partitions[ii] = builders[ii].getIndex();
                if (resetBuilders) {
                    builders[ii] = Index.FACTORY.getSequentialBuilder();
                } else {
                    builders[ii] = null;
                }
            }
            return partitions;
        }

//        public void showPivots() {
//            System.out.println("[" + IntStream.range(0, pivotValues.size()).mapToObj(pivotValues::get).map(ObjectPartitionKernel::format).collect(Collectors.joining(",")) + "]");
//            System.out.println("[" + IntStream.range(0, pivotKeys.size()).mapToObj(pivotKeys::get).map(Object::toString).collect(Collectors.joining(",")) + "]");
//        }

        public ObjectLongTuple [] getPivots() {
            return IntStream.range(0, pivotValues.size()).mapToObj(ii -> new ObjectLongTuple(pivotValues.get(ii), pivotKeys.get(ii))).toArray(ObjectLongTuple[]::new);
        }
    }

//    private static String format(Object last) {
//        if (last >= 'A' && last <= 'Z') {
//            return Object.toString(last);
//        }
//        return String.format("0x%04x", (int) last);
//    }

    public static PartitionKernelContext createContext(Index index, ColumnSource<Object> columnSource, int chunkSize, int nPartitions, boolean preserveEquality) {
        final PartitionKernelContext context = new PartitionKernelContext(chunkSize, nPartitions, preserveEquality);

        final WritableLongChunk<KeyIndices> tempPivotKeys = WritableLongChunk.makeWritableChunk(nPartitions * 3);
        final WritableObjectChunk<Object, Any> tempPivotValues = WritableObjectChunk.makeWritableChunk(nPartitions * 3);

        samplePivots(index, nPartitions, tempPivotKeys, tempPivotValues, columnSource);

        // copy from the oversized chunk, which was used for sorting into the chunk which we will use for our binary searches
        for (int ii = 0; ii < tempPivotKeys.size(); ++ii) {
            context.pivotKeys.set(ii, tempPivotKeys.get(ii));
            context.pivotValues.set(ii, tempPivotValues.get(ii));
        }

        return context;
    }

    // the sample pivots function could be smarter; in that if we are reading a block, there is a strong argument to
    // sample the entirety of the relevant values within the block from disk.  We might also want to do a complete
    // linear pass so that we can determine ideal pivots (or maybe if a radix based approach is better).
    private static void samplePivots(Index index, int nPartitions, WritableLongChunk<KeyIndices> pivotKeys, WritableObjectChunk<Object, Any> pivotValues, ColumnSource<Object> columnSource) {
        pivotKeys.setSize(0);
        final int pivotsRequired = nPartitions - 1;
        final int samplesRequired = pivotsRequired * 3;
        PartitionUtilities.sampleIndexKeys(0, index, samplesRequired, pivotKeys);

        final Index.SequentialBuilder builder = Index.FACTORY.getSequentialBuilder();
        for (int ii = 0; ii < pivotKeys.size(); ++ii) {
            builder.appendKey(pivotKeys.get(ii));
        }
        final Index indexOfPivots = builder.getIndex();
        try (final ColumnSource.FillContext context = columnSource.makeFillContext(samplesRequired)) {
            columnSource.fillChunk(context, pivotValues, indexOfPivots);
        }

        try (final LongSortKernel sortContext = ObjectLongTimsortKernel.createContext(samplesRequired)) {
            sortContext.sort(pivotKeys, pivotValues);
        }

        // now we have a thing that is sorted, we pick every third thing, starting with the second
        int ii, jj;
        for (ii = 0, jj = 1; jj < pivotKeys.size(); ii++, jj += 3) {
            pivotKeys.set(ii, pivotKeys.get(jj));
            pivotValues.set(ii, pivotValues.get(jj));
        }
        pivotKeys.setSize(ii);
    }

    /**
     * After we have created the context, we can determine what things are in a partition.
     *
     * @param context our context, containing the pivots
     * @param indexKeys a chunk of index keys to partition
     * @param values  a chunk of values that go with the index keys
     */
    public static void partition(PartitionKernelContext context, LongChunk<KeyIndices> indexKeys, ObjectChunk values) {
        final int accumulatedChunkSize = context.chunkSize;
        for (int ii = 0; ii < values.size(); ii += accumulatedChunkSize) {
            final int last = Math.min(values.size(), ii + accumulatedChunkSize);
            for (int jj = ii; jj < last; jj++) {
                // find value in the context's pivotKeys
                final Object searchValue = values.get(jj);
                final long searchKey = indexKeys.get(jj);

                final int partition;
                if (context.preserveEquality) {
                    partition = binarySearchPreserve(context.pivotValues, 0, context.pivotValues.size(), searchValue);
                } else {
                    partition = binarySearchTieIndex(context.pivotValues, context.pivotKeys, 0, context.pivotValues.size(), searchValue, searchKey);
                }
                context.accumulatedKeys[partition].add(searchKey);
                if (context.accumulatedKeys[partition].size() == accumulatedChunkSize) {
                    flushToBuilder(context, partition);
                }
            }
        }
        flushAllToBuilders(context);
    }

    private static void flushAllToBuilders(PartitionKernelContext context) {
        for (int ii = 0; ii < context.accumulatedKeys.length; ++ii) {
            flushToBuilder(context, ii);
        }
    }

    private static void flushToBuilder(PartitionKernelContext context, int partition) {
        final Index.SequentialBuilder builder = context.builders[partition];
        final WritableLongChunk<KeyIndices> partitionKeys = context.accumulatedKeys[partition];
        final int chunkSize = partitionKeys.size();
        for (int ii = 0; ii < chunkSize; ++ii) {
            builder.appendKey(partitionKeys.get(ii));
        }
        partitionKeys.setSize(0);
    }

    private static int binarySearchPreserve(ObjectChunk pivotValues, int lo, int hi, Object searchValue) {
        while (lo != hi) {
            final int mid = (lo + hi) / 2;
            final Object compareValue = pivotValues.get(mid);
            if (eq(searchValue, compareValue)) {
                return mid * 2 + 1;
            } else if (lt(searchValue , compareValue)) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo * 2;
    }

    private static int binarySearchTieIndex(ObjectChunk pivotValues, LongChunk<KeyIndices> pivotKeys, int lo, int hi, Object searchValue, long searchKey) {
        while (lo != hi) {
            final int mid = (lo + hi) / 2;
            final Object compareValue = pivotValues.get(mid);
            if (eq(searchValue, compareValue)) {
                // we must break the tie using the pivotKeys, which is guaranteed to be unique
                final long compareKey = pivotKeys.get(mid);
                if (searchKey <= compareKey) {
                    hi = mid;
                } else {
                    lo = mid + 1;
                }
            } else if (lt(searchValue, compareValue)) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    // region comparison functions
    // ascending comparison
    private static int doComparison(Object lhs, Object rhs) {
       if (lhs == rhs) {
            return 0;
        }
        if (lhs == null) {
            return -1;
        }
        if (rhs == null) {
            return 1;
        }
        //noinspection unchecked
        return ((Comparable)lhs).compareTo(rhs);
    }

    // endregion comparison functions

    // region equality function
    private static boolean eq(Object lhs, Object rhs) {
        return Objects.equals(lhs, rhs);
    }
    // endregion equality function

    private static boolean lt(Object lhs, Object rhs) {
        return doComparison(lhs, rhs) < 0;
    }
}
