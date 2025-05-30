//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
// ****** AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY
// ****** Edit CharRollingWAvgOperator and run "./gradlew replicateUpdateBy" to regenerate
//
// @formatter:off
package io.deephaven.engine.table.impl.updateby.rollingwavg;

import io.deephaven.chunk.FloatChunk;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.impl.MatchPair;
import io.deephaven.engine.table.impl.updateby.UpdateByOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.deephaven.util.QueryConstants.NULL_FLOAT;
import static io.deephaven.util.QueryConstants.NULL_DOUBLE;

public class FloatRollingWAvgOperator extends BasePrimitiveRollingWAvgOperator {
    // region extra-fields
    // endregion extra-fields

    protected class Context extends BasePrimitiveRollingWAvgOperator.Context {
        protected FloatChunk<? extends Values> influencerValuesChunk;

        protected Context(int affectedChunkSize, int influencerChunkSize) {
            super(affectedChunkSize, influencerChunkSize);
        }

        @Override
        public void setValueChunks(@NotNull final Chunk<? extends Values>[] valueChunks) {
            super.setValueChunks(valueChunks);
            influencerValuesChunk = valueChunks[0].asFloatChunk();
        }

        @Override
        public void push(int pos, int count) {
            windowValues.ensureRemaining(count);
            windowWeightValues.ensureRemaining(count);

            for (int ii = 0; ii < count; ii++) {
                final float val = influencerValuesChunk.get(pos + ii);
                final double weight = influencerWeightValuesChunk.get(pos + ii);

                if (val == NULL_FLOAT || weight == NULL_DOUBLE) {
                    windowValues.addUnsafe(NULL_DOUBLE);
                    windowWeightValues.addUnsafe(NULL_DOUBLE);
                    nullCount++;
                } else {
                    // Compute the product and add to the agg buffer.
                    final double weightedVal = weight * val;
                    windowValues.addUnsafe(weightedVal);
                    windowWeightValues.addUnsafe(weight);
                }
            }
        }
    }

    @NotNull
    @Override
    public UpdateByOperator.Context makeUpdateContext(final int affectedChunkSize, final int influencerChunkSize) {
        return new Context(affectedChunkSize, influencerChunkSize);
    }

    public FloatRollingWAvgOperator(
            @NotNull final MatchPair pair,
            @NotNull final String[] affectingColumns,
            @Nullable final String timestampColumnName,
            final long reverseWindowScaleUnits,
            final long forwardWindowScaleUnits,
            @NotNull final String weightColumnName
    // region extra-constructor-args
    // endregion extra-constructor-args
    ) {
        super(pair, affectingColumns, timestampColumnName, reverseWindowScaleUnits, forwardWindowScaleUnits,
                weightColumnName);
        // region constructor
        // endregion constructor
    }

    @Override
    public UpdateByOperator copy() {
        return new FloatRollingWAvgOperator(
                pair,
                affectingColumns,
                timestampColumnName,
                reverseWindowScaleUnits,
                forwardWindowScaleUnits,
                weightColumnName
        // region extra-copy-args
        // endregion extra-copy-args
        );
    }
}
