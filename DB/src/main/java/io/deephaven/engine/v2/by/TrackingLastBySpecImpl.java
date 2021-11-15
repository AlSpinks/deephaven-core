/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2.by;

public class TrackingLastBySpecImpl extends IterativeIndexSpec {
    public TrackingLastBySpecImpl() {
        this(false, false, 0);
    }

    private TrackingLastBySpecImpl(boolean lowestRollup, boolean secondRollup, int rollupColumnIdentifier) {
        super(lowestRollup, secondRollup, rollupColumnIdentifier);
    }

    private static final AggregationMemoKey TRACKING_LASTBY_INSTANCE = new AggregationMemoKey() {};

    @Override
    public AggregationMemoKey getMemoKey() {
        return TRACKING_LASTBY_INSTANCE;
    }


    @Override
    ReaggregatableStatefactory forRollup() {
        return new TrackingLastBySpecImpl(true, false, 0);
    }

    /**
     * Sort the results by the original rowSet when aggregating on state.
     */
    @Override
    ReaggregatableStatefactory rollupFactory() {
        return new SortedFirstOrLastByFactoryImpl(false, false, true, rollupColumnIdentifier,
                REDIRECTION_INDEX_PREFIX + rollupColumnIdentifier + AggregationFactory.ROLLUP_COLUMN_SUFFIX);
    }

    @Override
    public String toString() {
        if (!lowestRollup && !secondRollup) {
            return "TrackingLastByStateFactory";
        } else {
            return "TrackingLastByStateFactory{" +
                    "lowestRollup=" + lowestRollup +
                    ", secondRollup=" + secondRollup +
                    ", rollupColumnIdentifier=" + rollupColumnIdentifier +
                    '}';
        }
    }
}
