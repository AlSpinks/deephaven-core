/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2.sources;

public interface TableSource {
    ColumnSource getColumnSource(String columnName);
}
