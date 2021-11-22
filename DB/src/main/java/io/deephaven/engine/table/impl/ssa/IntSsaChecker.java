/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharSsaChecker and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.engine.table.impl.ssa;

import io.deephaven.base.verify.Assert;
import io.deephaven.engine.chunk.util.hashing.IntChunkEquals;
import io.deephaven.engine.chunk.util.hashing.LongChunkEquals;
import io.deephaven.engine.chunk.Attributes;
import io.deephaven.engine.chunk.Attributes.RowKeys;
import io.deephaven.engine.chunk.Attributes.Values;
import io.deephaven.engine.chunk.IntChunk;
import io.deephaven.engine.chunk.Chunk;
import io.deephaven.engine.chunk.LongChunk;
import io.deephaven.engine.table.impl.util.ChunkUtils;

public class IntSsaChecker implements SsaChecker {
    static IntSsaChecker INSTANCE = new IntSsaChecker();

    private IntSsaChecker() {} // static use only

    @Override
    public void checkSsa(SegmentedSortedArray ssa, Chunk<? extends Values> valueChunk, LongChunk<? extends RowKeys> tableIndexChunk) {
        checkSsa((IntSegmentedSortedArray)ssa, valueChunk.asIntChunk(), tableIndexChunk);
    }

    static void checkSsa(IntSegmentedSortedArray ssa, IntChunk<? extends Values> valueChunk, LongChunk<? extends Attributes.RowKeys> tableIndexChunk) {
        ssa.validateInternal();

        //noinspection unchecked
        final IntChunk<Values> resultChunk = (IntChunk) ssa.asIntChunk();
        final LongChunk<RowKeys> indexChunk = ssa.keyIndicesChunk();

        Assert.eq(valueChunk.size(), "valueChunk.size()", resultChunk.size(), "resultChunk.size()");
        Assert.eq(tableIndexChunk.size(), "tableIndexChunk.size()", indexChunk.size(), "indexChunk.size()");

        if (!IntChunkEquals.equalReduce(resultChunk, valueChunk)) {
            final StringBuilder messageBuilder = new StringBuilder("Values do not match:\n");
            messageBuilder.append("Result Values:\n").append(ChunkUtils.dumpChunk(resultChunk)).append("\n");
            messageBuilder.append("Table Values:\n").append(ChunkUtils.dumpChunk(valueChunk)).append("\n");;

            for (int ii = 0; ii < resultChunk.size(); ++ii) {
                if (!eq(resultChunk.get(ii), valueChunk.get(ii))) {
                    messageBuilder.append("First difference at ").append(ii).append(("\n"));
                    break;
                }
            }

            throw new SsaCheckException(messageBuilder.toString());
        }
        if (!LongChunkEquals.equalReduce(indexChunk, tableIndexChunk)) {
            final StringBuilder messageBuilder = new StringBuilder("Values do not match:\n");
            messageBuilder.append("Result:\n").append(ChunkUtils.dumpChunk(resultChunk)).append("\n");
            messageBuilder.append("Values:\n").append(ChunkUtils.dumpChunk(valueChunk)).append("\n");;

            messageBuilder.append("Result row keys:\n").append(ChunkUtils.dumpChunk(indexChunk)).append("\n");
            messageBuilder.append("Table row keys:\n").append(ChunkUtils.dumpChunk(tableIndexChunk)).append("\n");;

            for (int ii = 0; ii < indexChunk.size(); ++ii) {
                if (indexChunk.get(ii) != tableIndexChunk.get(ii)) {
                    messageBuilder.append("First difference at ").append(ii).append(("\n"));
                    break;
                }
            }

            throw new SsaCheckException(messageBuilder.toString());
        }
    }

    private static boolean eq(int lhs, int rhs) {
        // region equality function
        return lhs == rhs;
        // endregion equality function
    }
}
