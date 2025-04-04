//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.rowset.impl;

import io.deephaven.engine.rowset.RowSequence;
import io.deephaven.chunk.LongChunk;
import io.deephaven.engine.rowset.RowSequenceFactory;

public class RowSequenceRowKeysChunkImplTest extends RowSequenceTestBase {

    @Override
    protected RowSequence create(long... values) {
        return RowSequenceFactory.wrapRowKeysChunkAsRowSequence(LongChunk.chunkWrap(values));
    }
}
