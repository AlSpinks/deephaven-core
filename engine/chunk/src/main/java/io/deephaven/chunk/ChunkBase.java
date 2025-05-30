//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.chunk;

import io.deephaven.chunk.attributes.Any;


/**
 * A generic object intended to serve as a thin wrapper around an array region.
 */
public abstract class ChunkBase<ATTR extends Any> implements Chunk<ATTR> {
    /**
     * The Chunk's storage is the sub-range of the underlying array defined by [offset, offset + capacity). It is
     * illegal to access the underlying array outside of this range.
     */
    int offset;
    int capacity;
    /**
     * Useful data data in the chunk is in the sub-range of the underlying array defined by [offset, offset + size). It
     * is illegal to set size < 0 or size > capacity.
     */
    int size;

    ChunkBase(int arrayLength, int offset, int capacity) {
        ChunkHelpers.checkArrayArgs(arrayLength, offset, capacity);
        this.offset = offset;
        this.capacity = capacity;
        this.size = capacity;
    }

    @Override
    public final int size() {
        return size;
    }

    /**
     * DO NOT CALL THIS INTERNAL METHOD. If you want to set a size, call {@link WritableChunk#setSize}. That method is
     * the only legal caller of this method in the entire system.
     */
    public final void internalSetSize(int newSize, long password) {
        if (password != -7025656774858671822L) {
            throw new UnsupportedOperationException(
                    "DO NOT CALL THIS INTERNAL METHOD. Instead call WritableChunk.setSize()");
        }
        if (newSize < 0 || newSize > capacity) {
            throw new IllegalArgumentException("size " + newSize + " is incompatible with capacity " + capacity);
        }

        this.size = newSize;
    }

    /**
     * DO NOT CALL THIS INTERNAL METHOD. Call {@link WritableChunk#capacity()} That method is the only legal caller of
     * this method in the entire system.
     */
    public final int internalCapacity(long password) {
        if (password != 1837055652467547514L) {
            throw new UnsupportedOperationException(
                    "DO NOT CALL THIS INTERNAL METHOD. Instead call WritableChunk.capacity()");
        }
        return capacity;
    }
}
