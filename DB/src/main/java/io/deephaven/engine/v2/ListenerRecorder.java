package io.deephaven.engine.v2;

import io.deephaven.engine.v2.sources.LogicalClock;
import io.deephaven.engine.v2.utils.RowSetFactoryImpl;
import io.deephaven.engine.v2.utils.TrackingMutableRowSet;
import io.deephaven.engine.v2.utils.IndexShiftData;
import io.deephaven.engine.v2.utils.IndexShiftDataExpander;

/**
 * A listener recorder stores references to added, removed, modified, and shifted indices; and then notifies a
 * {@link MergedListener} that a change has occurred. The combination of a {@link ListenerRecorder} and
 * {@link MergedListener} should be used when a table has multiple sources, such that each table can process all of it's
 * dependencies at once and fire a single notification to its children.
 */
public class ListenerRecorder extends BaseTable.ShiftAwareListenerImpl {
    protected final String logPrefix;
    protected final boolean isRefreshing;

    private MergedListener mergedListener;

    private long notificationStep = -1;
    private Update update;

    public ListenerRecorder(String description, DynamicTable parent, DynamicTable dependent) {
        super(description, parent, dependent);
        this.logPrefix = System.identityHashCode(this) + ": " + description + "Listener Recorder: ";
        this.isRefreshing = parent.isRefreshing();
    }

    boolean isRefreshing() {
        return isRefreshing;
    }

    public void release() {
        if (update != null) {
            update.release();
            update = null;
        }
    }

    @Override
    public void onUpdate(final Update upstream) {
        this.update = upstream.acquire();
        this.notificationStep = LogicalClock.DEFAULT.currentStep();

        // notify the downstream listener merger
        if (mergedListener == null) {
            throw new IllegalStateException("Merged listener not set!");
        }

        mergedListener.notifyChanges();
    }

    public boolean recordedVariablesAreValid() {
        return notificationStep == LogicalClock.DEFAULT.currentStep();
    }

    public void setMergedListener(MergedListener mergedListener) {
        this.mergedListener = mergedListener;
    }

    public long getNotificationStep() {
        return notificationStep;
    }

    public TrackingMutableRowSet getAdded() {
        return recordedVariablesAreValid() ? update.added : RowSetFactoryImpl.INSTANCE.getEmptyRowSet();
    }

    public TrackingMutableRowSet getRemoved() {
        return recordedVariablesAreValid() ? update.removed : RowSetFactoryImpl.INSTANCE.getEmptyRowSet();
    }

    public TrackingMutableRowSet getModified() {
        return recordedVariablesAreValid() ? update.modified : RowSetFactoryImpl.INSTANCE.getEmptyRowSet();
    }

    public TrackingMutableRowSet getModifiedPreShift() {
        return recordedVariablesAreValid() ? update.getModifiedPreShift() : RowSetFactoryImpl.INSTANCE.getEmptyRowSet();
    }

    public IndexShiftData getShifted() {
        return recordedVariablesAreValid() ? update.shifted : IndexShiftData.EMPTY;
    }

    public ModifiedColumnSet getModifiedColumnSet() {
        return recordedVariablesAreValid() ? update.modifiedColumnSet : null;
    }

    public Update getUpdate() {
        return recordedVariablesAreValid() ? update : null;
    }

    /**
     * The caller is responsible for closing the {@link IndexShiftDataExpander}.
     * 
     * @return a backwards compatible version of added / removed / modified that account for shifting
     */
    public IndexShiftDataExpander getExpandedARM() {
        return recordedVariablesAreValid() ? new IndexShiftDataExpander(update, getParent().getIndex())
                : IndexShiftDataExpander.EMPTY;
    }
}
