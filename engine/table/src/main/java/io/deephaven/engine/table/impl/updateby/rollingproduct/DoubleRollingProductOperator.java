//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
// ****** AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY
// ****** Edit FloatRollingProductOperator and run "./gradlew replicateUpdateBy" to regenerate
//
// @formatter:off
package io.deephaven.engine.table.impl.updateby.rollingproduct;

import io.deephaven.base.ringbuffer.AggregatingDoubleRingBuffer;
import io.deephaven.base.verify.Assert;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.DoubleChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.engine.table.impl.MatchPair;
import io.deephaven.engine.table.impl.updateby.UpdateByOperator;
import io.deephaven.engine.table.impl.updateby.internal.BaseDoubleUpdateByOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.deephaven.util.QueryConstants.NULL_DOUBLE;
import static io.deephaven.util.QueryConstants.NULL_DOUBLE;

public class DoubleRollingProductOperator extends BaseDoubleUpdateByOperator {
    private static final int BUFFER_INITIAL_SIZE = 64;

    protected class Context extends BaseDoubleUpdateByOperator.Context {
        protected DoubleChunk<? extends Values> doubleInfluencerValuesChunk;
        protected AggregatingDoubleRingBuffer buffer;

        private int zeroCount;
        private int nanCount;
        private int infCount;

        @SuppressWarnings("unused")
        protected Context(final int affectedChunkSize, final int influencerChunkSize) {
            super(affectedChunkSize);
            buffer = new AggregatingDoubleRingBuffer(BUFFER_INITIAL_SIZE,
                    1L,
                    (a, b) -> a * b, // tree function
                    (a, b) -> { // value function
                        if (a == NULL_DOUBLE && b == NULL_DOUBLE) {
                            return 1L; // identity val
                        } else if (a == NULL_DOUBLE) {
                            return b;
                        } else if (b == NULL_DOUBLE) {
                            return a;
                        }
                        return a * b;
                    },
                    true);
            zeroCount = 0;
            nanCount = 0;
            infCount = 0;
        }

        @Override
        public void close() {
            super.close();
            buffer = null;
        }


        @Override
        public void setValueChunks(@NotNull final Chunk<? extends Values>[] valueChunks) {
            doubleInfluencerValuesChunk = valueChunks[0].asDoubleChunk();
        }

        @Override
        public void push(int pos, int count) {
            buffer.ensureRemaining(count);

            for (int ii = 0; ii < count; ii++) {
                double val = doubleInfluencerValuesChunk.get(pos + ii);

                if (val == NULL_DOUBLE) {
                    buffer.addUnsafe(NULL_DOUBLE);
                    nullCount++;
                } else {
                    buffer.addUnsafe(val);
                    if (val == 0) {
                        zeroCount++;
                    } else if (Double.isNaN(val)) {
                        nanCount++;
                    } else if (Double.isInfinite(val)) {
                        infCount++;
                    }
                }
            }
        }

        @Override
        public void pop(int count) {
            Assert.geq(buffer.size(), "buffer.size()", count);

            for (int ii = 0; ii < count; ii++) {
                double val = buffer.removeUnsafe();

                if (val == NULL_DOUBLE) {
                    nullCount--;
                } else if (Double.isNaN(val)) {
                    --nanCount;
                } else if (val == 0) {
                    --zeroCount;
                } else if (Double.isInfinite(val)) {
                    --infCount;
                }
            }
        }

        @Override
        public void writeToOutputChunk(int outIdx) {
            if (buffer.size() == nullCount) {
                outputValues.set(outIdx, NULL_DOUBLE);
            } else {
                if (nanCount > 0 || (infCount > 0 && zeroCount > 0)) {
                    // Output NaN without evaluating the buffer when the buffer is poisoned with NaNs or when we
                    // have an Inf * 0 case
                    outputValues.set(outIdx, Double.NaN);
                } else {
                    // When zeros are present, we can skip evaluating the buffer.
                    outputValues.set(outIdx, zeroCount > 0 ? 0.0 : buffer.evaluate());
                }
            }
        }

        @Override
        public void reset() {
            super.reset();
            zeroCount = 0;
            nanCount = 0;
            infCount = 0;
            buffer.clear();
        }
    }

    @NotNull
    @Override
    public UpdateByOperator.Context makeUpdateContext(final int affectedChunkSize, final int influencerChunkSize) {
        return new Context(affectedChunkSize, influencerChunkSize);
    }

    public DoubleRollingProductOperator(
            @NotNull final MatchPair pair,
            @NotNull final String[] affectingColumns,
            @Nullable final String timestampColumnName,
            final long reverseWindowScaleUnits,
            final long forwardWindowScaleUnits) {
        super(pair, affectingColumns, timestampColumnName, reverseWindowScaleUnits, forwardWindowScaleUnits, true);
    }

    @Override
    public UpdateByOperator copy() {
        return new DoubleRollingProductOperator(
                pair,
                affectingColumns,
                timestampColumnName,
                reverseWindowScaleUnits,
                forwardWindowScaleUnits);
    }
}
