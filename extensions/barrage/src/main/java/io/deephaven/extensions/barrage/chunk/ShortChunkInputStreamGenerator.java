/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharChunkInputStreamGenerator and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.extensions.barrage.chunk;

import gnu.trove.iterator.TLongIterator;
import io.deephaven.extensions.barrage.BarrageSubscriptionOptions;
import io.deephaven.db.v2.sources.chunk.ShortChunk;
import io.deephaven.util.QueryConstants;
import com.google.common.io.LittleEndianDataOutputStream;
import io.deephaven.UncheckedDeephavenException;
import io.deephaven.db.util.LongSizedDataStructure;
import io.deephaven.db.v2.sources.chunk.Attributes;
import io.deephaven.db.v2.sources.chunk.Chunk;
import io.deephaven.db.v2.sources.chunk.WritableShortChunk;
import io.deephaven.db.v2.sources.chunk.WritableLongChunk;
import io.deephaven.db.v2.utils.Index;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class ShortChunkInputStreamGenerator extends BaseChunkInputStreamGenerator<ShortChunk<Attributes.Values>> {
    private static final String DEBUG_NAME = "ShortChunkInputStreamGenerator";

    ShortChunkInputStreamGenerator(final ShortChunk<Attributes.Values> chunk, final int elementSize) {
        super(chunk, elementSize);
    }

    @Override
    public DrainableColumn getInputStream(final BarrageSubscriptionOptions options, final @Nullable Index subset) {
        return new ShortChunkInputStream(options, subset);
    }

    private class ShortChunkInputStream extends BaseChunkInputStream {
        private ShortChunkInputStream(final BarrageSubscriptionOptions options, final Index subset) {
            super(chunk, options, subset);
        }

        private int cachedNullCount = - 1;

        @Override
        public int nullCount() {
            if (options.useDeephavenNulls()) {
                return 0;
            }
            if (cachedNullCount == -1) {
                cachedNullCount = 0;
                subset.forAllLongs(row -> {
                    if (chunk.get((int) row) == QueryConstants.NULL_SHORT) {
                        ++cachedNullCount;
                    }
                });
            }
            return cachedNullCount;
        }

        @Override
        public void visitFieldNodes(final FieldNodeListener listener) {
            listener.noteLogicalFieldNode(subset.intSize(DEBUG_NAME), nullCount());
        }

        @Override
        public void visitBuffers(final BufferListener listener) {
            // validity
            listener.noteLogicalBuffer(sendValidityBuffer() ? getValidityMapSerializationSizeFor(subset.intSize()) : 0);
            // payload
            long length = elementSize * subset.size();
            final long bytesExtended = length & REMAINDER_MOD_8_MASK;
            if (bytesExtended > 0) {
                length += 8 - bytesExtended;
            }
            listener.noteLogicalBuffer(length);
        }

        @Override
        public int drainTo(final OutputStream outputStream) throws IOException {
            if (read || subset.isEmpty()) {
                return 0;
            }

            long bytesWritten = 0;
            read = true;
            try (final LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(outputStream)) {
                // write the validity array with LSB indexing
                if (sendValidityBuffer()) {
                    final SerContext context = new SerContext();
                    final Runnable flush = () -> {
                        try {
                            dos.writeLong(context.accumulator);
                        } catch (final IOException e) {
                            throw new UncheckedDeephavenException("Unexpected exception while draining data to OutputStream: ", e);
                        }
                        context.accumulator = 0;
                        context.count = 0;
                    };
                    subset.forAllLongs(row -> {
                        if (chunk.get((int) row) != QueryConstants.NULL_SHORT) {
                            context.accumulator |= 1L << context.count;
                        }
                        if (++context.count == 64) {
                            flush.run();
                        }
                    });
                    if (context.count > 0) {
                        flush.run();
                    }

                    bytesWritten += getValidityMapSerializationSizeFor(subset.intSize());
                }

                // write the included values
                subset.forAllLongs(row -> {
                    try {
                        final short val = chunk.get((int) row);
                        dos.writeShort(val);
                    } catch (final IOException e) {
                        throw new UncheckedDeephavenException("Unexpected exception while draining data to OutputStream: ", e);
                    }
                });

                bytesWritten += elementSize * subset.size();
                final long bytesExtended = bytesWritten & REMAINDER_MOD_8_MASK;
                if (bytesExtended > 0) {
                    bytesWritten += 8 - bytesExtended;
                    dos.write(PADDING_BUFFER, 0, (int)(8 - bytesExtended));
                }
            }
            return LongSizedDataStructure.intSize("ShortChunkInputStreamGenerator", bytesWritten);
        }
    }

    static Chunk<Attributes.Values> extractChunkFromInputStream(
            final int elementSize, final BarrageSubscriptionOptions options,
            final Iterator<FieldNodeInfo> fieldNodeIter,
            final TLongIterator bufferInfoIter,
            final DataInput is) throws IOException {

        final FieldNodeInfo nodeInfo = fieldNodeIter.next();
        final long validityBuffer = bufferInfoIter.next();
        final long payloadBuffer = bufferInfoIter.next();

        final WritableShortChunk<Attributes.Values> chunk = WritableShortChunk.makeWritableChunk(nodeInfo.numElements);

        if (nodeInfo.numElements == 0) {
            return chunk;
        }

        final int numValidityLongs = options.useDeephavenNulls() ? 0 : (nodeInfo.numElements + 63) / 64;
        try (final WritableLongChunk<Attributes.Values> isValid = WritableLongChunk.makeWritableChunk(numValidityLongs)) {
            if (options.useDeephavenNulls() && validityBuffer != 0) {
                throw new IllegalStateException("validity buffer is non-empty, but is unnecessary");
            }
            int jj = 0;
            for (; jj < Math.min(numValidityLongs, validityBuffer / 8); ++jj) {
                isValid.set(jj, is.readLong());
            }
            final long valBufRead = jj * 8L;
            if (valBufRead < validityBuffer) {
                is.skipBytes(LongSizedDataStructure.intSize(DEBUG_NAME, validityBuffer - valBufRead));
            }
            // we support short validity buffers
            for (; jj < numValidityLongs; ++jj) {
                isValid.set(jj, -1); // -1 is bit-wise representation of all ones
            }
            // consumed entire validity buffer by here

            final long payloadRead = (long) nodeInfo.numElements * elementSize;
            if (payloadBuffer < payloadRead) {
                throw new IllegalStateException("payload buffer is too short for expected number of elements");
            }

            if (options.useDeephavenNulls()) {
                for (int ii = 0; ii < nodeInfo.numElements; ++ii) {
                    chunk.set(ii, is.readShort());
                }
            } else {
                long nextValid = 0;
                for (int ii = 0; ii < nodeInfo.numElements; ++ii) {
                    if ((ii % 64) == 0) {
                        nextValid = isValid.get(ii / 64);
                    }
                    final short value;
                    if ((nextValid & 0x1) == 0x0) {
                        value = QueryConstants.NULL_SHORT;
                        is.skipBytes(elementSize);
                    } else {
                        value = is.readShort();
                    }
                    nextValid >>= 1;
                    chunk.set(ii, value);
                }
            }

            final long overhangPayload = payloadBuffer - payloadRead;
            if (overhangPayload > 0) {
                is.skipBytes(LongSizedDataStructure.intSize(DEBUG_NAME, overhangPayload));
            }
        }

        chunk.setSize(nodeInfo.numElements);
        return chunk;
    }
}