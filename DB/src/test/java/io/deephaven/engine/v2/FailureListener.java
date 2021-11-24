package io.deephaven.engine.v2;

import io.deephaven.engine.table.TableUpdate;
import io.deephaven.engine.table.impl.perf.UpdatePerformanceTracker;
import junit.framework.TestCase;

public class FailureListener extends InstrumentedTableUpdateListener {
    public FailureListener() {
        super("Fuzzer Failure ShiftObliviousListener");
    }

    @Override
    public void onUpdate(final TableUpdate upstream) {}

    @Override
    public void onFailureInternal(Throwable originalException,
            UpdatePerformanceTracker.Entry sourceEntry) {
        originalException.printStackTrace();
        TestCase.fail(originalException.getMessage());
    }
}
