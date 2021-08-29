/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit UngroupedCharArrayColumnSource and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2.sources;

import static io.deephaven.util.QueryConstants.NULL_SHORT;

public class UngroupedShortArrayColumnSource extends UngroupedColumnSource<Short> implements MutableColumnSourceGetDefaults.ForShort {
    private ColumnSource<short[]> innerSource;

    @Override
    public Class<?> getComponentType() {
        return null;
    }


    public UngroupedShortArrayColumnSource(ColumnSource<short[]> innerSource) {
        super(Short.class);
        this.innerSource = innerSource;
    }

    @Override
    public short getShort(long index) {
        if (index < 0) {
            return NULL_SHORT;
        }
        long segment = index>>base;
        int offset = (int) (index & ((1<<base) - 1));
        short[] array = innerSource.get(segment);
        if(array == null || offset >= array.length) {
            return NULL_SHORT;
        }
        return array[offset];
    }

    @Override
    public short getPrevShort(long index) {
        if (index < 0) {
            return NULL_SHORT;
        }
        long segment = index>> getPrevBase();
        int offset = (int) (index & ((1<< getPrevBase()) - 1));
        short[] array = innerSource.getPrev(segment);
        if(array == null || offset >= array.length) {
            return NULL_SHORT;
        }
        return array[offset];
    }

    @Override
    public boolean isImmutable() {
        return innerSource.isImmutable();
    }
}
