/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2;

import io.deephaven.engine.tables.live.LiveTable;
import io.deephaven.engine.v2.utils.UpdatePerformanceTracker;

public abstract class InstrumentedLiveTable implements LiveTable {

    protected final UpdatePerformanceTracker.Entry entry;

    public InstrumentedLiveTable(String description) {
        this.entry = UpdatePerformanceTracker.getInstance().getEntry(description);
    }

    @Override
    public final void refresh() {
        entry.onUpdateStart();
        try {
            instrumentedRefresh();
        } finally {
            entry.onUpdateEnd();
        }
    }

    protected abstract void instrumentedRefresh();
}
