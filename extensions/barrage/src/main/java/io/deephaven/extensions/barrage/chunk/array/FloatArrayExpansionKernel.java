//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
// ****** AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY
// ****** Edit CharArrayExpansionKernel and run "./gradlew replicateBarrageUtils" to regenerate
//
// @formatter:off
package io.deephaven.extensions.barrage.chunk.array;

import io.deephaven.chunk.FloatChunk;
import io.deephaven.chunk.Chunk;
import io.deephaven.chunk.IntChunk;
import io.deephaven.chunk.ObjectChunk;
import io.deephaven.chunk.WritableFloatChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.WritableIntChunk;
import io.deephaven.chunk.WritableObjectChunk;
import io.deephaven.chunk.attributes.Any;
import io.deephaven.chunk.attributes.ChunkLengths;
import io.deephaven.chunk.attributes.ChunkPositions;
import io.deephaven.extensions.barrage.chunk.BaseChunkReader;
import io.deephaven.util.datastructures.LongSizedDataStructure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatArrayExpansionKernel implements ArrayExpansionKernel<float[]> {
    public static final FloatArrayExpansionKernel INSTANCE = new FloatArrayExpansionKernel();

    private static final String DEBUG_NAME = "FloatArrayExpansionKernel";
    private static final float[] ZERO_LEN_ARRAY = new float[0];

    @Override
    public <A extends Any> WritableChunk<A> expand(
            @NotNull final ObjectChunk<float[], A> source,
            final int fixedSizeLength,
            @Nullable final WritableIntChunk<ChunkPositions> offsetsDest) {
        if (source.size() == 0) {
            if (offsetsDest != null) {
                offsetsDest.setSize(0);
            }
            return WritableFloatChunk.makeWritableChunk(0);
        }

        long totalSize = 0;
        if (fixedSizeLength != 0) {
            totalSize = source.size() * (long) fixedSizeLength;
        } else {
            for (int ii = 0; ii < source.size(); ++ii) {
                final float[] row = source.get(ii);
                final int rowLen = row == null ? 0 : row.length;
                totalSize += rowLen;
            }
        }
        final WritableFloatChunk<A> result = WritableFloatChunk.makeWritableChunk(
                LongSizedDataStructure.intSize(DEBUG_NAME, totalSize));

        int lenWritten = 0;
        if (offsetsDest != null) {
            offsetsDest.setSize(source.size() + 1);
        }
        for (int ii = 0; ii < source.size(); ++ii) {
            final float[] row = source.get(ii);
            if (offsetsDest != null) {
                offsetsDest.set(ii, lenWritten);
            }
            int written = 0;
            if (row != null) {
                if (fixedSizeLength != 0) {
                    // limit length to fixedSizeLength
                    written = Math.min(row.length, fixedSizeLength);
                } else {
                    written = row.length;
                }
                // copy the row into the result
                result.copyFromArray(row, 0, lenWritten, written);
            }
            if (fixedSizeLength != 0) {
                final int toNull = LongSizedDataStructure.intSize(
                        DEBUG_NAME, Math.max(0, fixedSizeLength - written));
                if (toNull > 0) {
                    // fill the rest of the row with nulls
                    result.fillWithNullValue(lenWritten + written, toNull);
                    written += toNull;
                }
            }
            lenWritten += written;
        }
        if (offsetsDest != null) {
            offsetsDest.set(source.size(), lenWritten);
        }

        return result;
    }

    @Override
    public <A extends Any> WritableObjectChunk<float[], A> contract(
            @NotNull final Chunk<A> source,
            final int sizePerElement,
            @Nullable final IntChunk<ChunkPositions> offsets,
            @Nullable final IntChunk<ChunkLengths> lengths,
            @Nullable final WritableChunk<A> outChunk,
            final int outOffset,
            final int totalRows) {
        final int itemsInBatch = offsets == null
                ? source.size() / sizePerElement
                : (offsets.size() - (lengths == null ? 1 : 0));
        final FloatChunk<A> typedSource = source.asFloatChunk();
        final WritableObjectChunk<float[], A> result = BaseChunkReader.castOrCreateChunk(
                outChunk,
                outOffset,
                Math.max(totalRows, itemsInBatch),
                WritableObjectChunk::makeWritableChunk,
                WritableChunk::asWritableObjectChunk);

        for (int ii = 0; ii < itemsInBatch; ++ii) {
            final int offset = offsets == null ? ii * sizePerElement : offsets.get(ii);
            final int rowLen = computeSize(ii, sizePerElement, offsets, lengths);
            if (rowLen == 0) {
                result.set(outOffset + ii, ZERO_LEN_ARRAY);
            } else {
                final float[] row = new float[rowLen];
                typedSource.copyToArray(offset, row, 0, rowLen);
                result.set(outOffset + ii, row);
            }
        }

        return result;
    }
}
