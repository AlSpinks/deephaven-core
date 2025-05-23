//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.parquet.table.pagestore.topage;

import io.deephaven.chunk.attributes.Any;
import io.deephaven.vector.Vector;
import io.deephaven.chunk.ChunkType;
import io.deephaven.parquet.base.DataWithOffsets;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.nio.IntBuffer;

public class ToArrayPage<ATTR extends Any, RESULT, ARRAY_TYPE>
        extends ToPage.Wrap<ATTR, RESULT, ARRAY_TYPE[]> {

    private final Class<ARRAY_TYPE> nativeType;

    public static <ATTR extends Any, ARRAY_TYPE> ToPage<ATTR, ARRAY_TYPE[]> create(
            @NotNull final Class<ARRAY_TYPE> nativeType,
            @NotNull final Class<?> componentType,
            @NotNull final ToPage<ATTR, ?> toPage) {
        if (!nativeType.isArray()) {
            throw new IllegalArgumentException("Native type " + nativeType + " is not an array type.");
        }

        final Class<?> columnComponentType = toPage.getNativeComponentType();
        if (!componentType.isAssignableFrom(columnComponentType)) {
            throw new IllegalArgumentException("The component type " + componentType.getCanonicalName() + " for the" +
                    " array type " + nativeType.getCanonicalName() +
                    " is not compatible with the column's component type " + columnComponentType);
        }

        return new ToArrayPage<>(nativeType, toPage);
    }

    private ToArrayPage(@NotNull final Class<ARRAY_TYPE> nativeType, @NotNull final ToPage<ATTR, RESULT> toPage) {
        super(toPage);
        this.nativeType = nativeType;
    }

    @Override
    @NotNull
    public final Class<ARRAY_TYPE> getNativeType() {
        return nativeType;
    }

    @Override
    @NotNull
    public final ChunkType getChunkType() {
        return ChunkType.Object;
    }

    @NotNull
    @Override
    public final ARRAY_TYPE[] convertResult(Object object) {
        final DataWithOffsets dataWithOffsets = (DataWithOffsets) object;

        final Vector<?> dataWrapper = toPage.makeVector(toPage.convertResult(dataWithOffsets.materializeResult));
        final IntBuffer offsets = dataWithOffsets.offsets;

        // noinspection unchecked
        final ARRAY_TYPE[] to = (ARRAY_TYPE[]) Array.newInstance(nativeType, offsets.remaining());

        int lastOffset = 0;
        for (int vi = 0; vi < to.length; ++vi) {
            final int nextOffset = offsets.get();
            if (nextOffset == DataWithOffsets.NULL_OFFSET) {
                to[vi] = null;
            } else {
                // noinspection unchecked
                to[vi] = (ARRAY_TYPE) dataWrapper.subVector(lastOffset, nextOffset).toArray();
                lastOffset = nextOffset;
            }
        }

        return to;
    }
}
