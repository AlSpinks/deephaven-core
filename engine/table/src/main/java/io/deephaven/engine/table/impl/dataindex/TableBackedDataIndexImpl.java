package io.deephaven.engine.table.impl.dataindex;

import io.deephaven.api.ColumnName;
import io.deephaven.api.Pair;
import io.deephaven.base.verify.Assert;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.rowset.TrackingRowSet;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.impl.QueryTable;
import io.deephaven.engine.table.impl.by.AggregationControl;
import io.deephaven.engine.table.impl.by.AggregationProcessor;
import io.deephaven.engine.table.impl.by.AggregationRowLookup;
import io.deephaven.engine.table.impl.perf.QueryPerformanceRecorder;
import io.deephaven.engine.table.impl.sources.RowSetColumnSourceWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.stream.Collectors;

import static io.deephaven.engine.table.impl.by.AggregationProcessor.EXPOSED_GROUP_ROW_SETS;

/**
 * This class creates a data index for a table. The index is a table containing the key column(s) and the RowSets that
 * contain these values. DataIndexes may be loaded from storage or created in-memory using aggregations.
 */
public class TableBackedDataIndexImpl extends AbstractDataIndex {
    /** The table containing the index. Consists of sorted key column(s) and an associated RowSet column. */
    private Table indexTable;

    @NotNull
    private final QueryTable sourceTable;

    @NotNull
    private final WeakHashMap<ColumnSource<?>, String> keyColumnMap;

    @NotNull
    final String[] keyColumnNames;

    private AggregationRowLookup lookupFunction;

    private SoftReference<Table> cachedPrevTable = new SoftReference<>(null);
    private long cachedPrevTableStep = -1;

    private SoftReference<PositionLookup> cachedPrevPositionLookup = new SoftReference<>(null);
    private long cachedPrevPositionLookupStep = -1;

    public TableBackedDataIndexImpl(@NotNull final QueryTable sourceTable,
            @NotNull final String[] keyColumnNames) {

        this.sourceTable = sourceTable;
        this.keyColumnNames = keyColumnNames;
        List<ColumnSource<?>> keySources = Arrays.stream(keyColumnNames).map(sourceTable::getColumnSource)
                .collect(Collectors.toList());

        // Create an in-order reverse lookup map for the key columnn names.
        keyColumnMap = new WeakHashMap<>(keySources.size());
        for (int ii = 0; ii < keySources.size(); ii++) {
            final ColumnSource<?> keySource = keySources.get(ii);
            final String keyColumnName = keyColumnNames[ii];
            keyColumnMap.put(keySource, keyColumnName);
        }

        // We will defer the actual index creation until it is needed.
    }

    @Override
    public String[] keyColumnNames() {
        return keyColumnNames;
    }

    @Override
    public Map<ColumnSource<?>, String> keyColumnMap() {
        return keyColumnMap;
    }

    @Override
    public String rowSetColumnName() {
        return INDEX_COL_NAME;
    }

    @Override
    @NotNull
    public Table table(final boolean usePrev) {
        if (usePrev && isRefreshing()) {
            // Return the cached table if possible.
            final Table cached = cachedPrevTable.get();
            if (cached != null && cached.getUpdateGraph().clock().currentStep() == cachedPrevTableStep) {
                return cached;
            }

            // Get the live current table.
            final Table currentTable = table();

            // Return a table containing the previous values of the index table.
            final TrackingRowSet prevRowSet = currentTable.getRowSet().copyPrev().toTracking();
            final Map<String, ColumnSource<?>> prevColumnSourceMap = new LinkedHashMap<>();
            currentTable.getColumnSourceMap().forEach((columnName, columnSource) -> {
                if (columnName.equals(rowSetColumnName())) {
                    prevColumnSourceMap.put(columnName, RowSetColumnSourceWrapper
                            .from((ColumnSource<TrackingRowSet>) columnSource).getPrevSource());
                    return;
                }
                prevColumnSourceMap.put(columnName, columnSource.getPrevSource());
            });

            final Table result = new QueryTable(prevRowSet, prevColumnSourceMap);
            cachedPrevTable = new SoftReference<>(result);
            cachedPrevTableStep = currentTable.getUpdateGraph().clock().currentStep();

            return result;
        }

        if (indexTable == null) {
            // TODO: break the hard reference from the index table to the source table. Otherwise this index will keep
            // the source table from being garbage collected.

            // Create the index table, grouped by the key column sources.
            indexTable = QueryPerformanceRecorder
                    .withNugget("Build Table Backed Data Index [" + String.join(", ", keyColumnNames) + "]", () -> {
                        final Table groupedTable = sourceTable
                                .aggNoMemo(
                                        AggregationControl.IGNORE_GROUPING,
                                        AggregationProcessor.forExposeGroupRowSets(),
                                        false,
                                        null,
                                        ColumnName.from(keyColumnNames));

                        lookupFunction = AggregationProcessor.getRowLookup(groupedTable);
                        Assert.neqNull(lookupFunction, "AggregationRowLookup lookupFunction should never be null");

                        return groupedTable.renameColumns(
                                Collections.singleton(Pair.of(EXPOSED_GROUP_ROW_SETS, ColumnName.of(INDEX_COL_NAME))));
                    });
        }
        return indexTable;
    }

    @Override
    public @Nullable RowSetLookup rowSetLookup(final boolean usePrev) {
        if (usePrev && isRefreshing()) {
            final Table prevTable = table(true);
            final PositionLookup prevPositionLookup = positionLookup(true);
            return (Object key) -> {
                // Pass the object to the aggregation lookup, then return the row set at that position.
                final int position = prevPositionLookup.apply(key);
                final long rowKey = prevTable.getRowSet().get(position);
                return (RowSet) prevTable.getColumnSource(rowSetColumnName()).get(rowKey);
            };
        }
        return (Object key) -> {
            // Pass the object to the aggregation lookup, then return the row set at that position.
            final int position = lookupFunction.get(key);
            return (RowSet) indexTable.getColumnSource(rowSetColumnName()).get(position);
        };
    }

    @Override
    public @NotNull PositionLookup positionLookup(final boolean usePrev) {
        if (usePrev && isRefreshing()) {
            // Return a valid cached lookup function if possible.
            final Table currentTable = table();
            PositionLookup positionLookup = cachedPrevPositionLookup.get();
            if (positionLookup != null
                    && table().getUpdateGraph().clock().currentStep() == cachedPrevPositionLookupStep) {
                return positionLookup;
            }
            synchronized (this) {
                // Test again, in case another thread has already updated the cache.
                positionLookup = cachedPrevPositionLookup.get();
                if (positionLookup != null
                        && table().getUpdateGraph().clock().currentStep() == cachedPrevPositionLookupStep) {
                    return positionLookup;
                }

                final Table prevTable = table(true);

                final PositionLookup newLookup = buildPositionLookup(prevTable, keyColumnNames);
                cachedPrevPositionLookup = new SoftReference<>(newLookup);
                cachedPrevPositionLookupStep = currentTable.getUpdateGraph().clock().currentStep();

                return newLookup;
            }
        }
        return (Object key) -> {
            // Pass the object to the aggregation lookup, then return the resulting position
            return lookupFunction.get(key);
        };
    }

    @Override
    public boolean isRefreshing() {
        return sourceTable.isRefreshing();
    }

    @Override
    public Table baseIndexTable() {
        return table();
    }

    @Override
    public boolean validate() {
        return true;
    }
}

