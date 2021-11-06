/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2.utils;

import io.deephaven.base.Function;
import io.deephaven.engine.tables.Table;
import io.deephaven.engine.tables.live.LiveTable;
import io.deephaven.engine.tables.live.UpdateGraphProcessor;
import io.deephaven.engine.v2.QueryTable;
import io.deephaven.engine.v2.BaseTable;
import io.deephaven.engine.v2.sources.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An abstract table that represents the result of a function.
 *
 * The table will run by regenerating the full values (using the tableGenerator Function passed in). The resultant
 * table's values are copied into the result table and appropriate listener notifications are fired.
 *
 * All of the rows in the output table are modified on every tick, even if no actual changes occurred. The output table
 * also has a contiguous rowSet.
 *
 * The generator function must produce a V2 table, and the table definition must not change between invocations.
 *
 * If you are transforming a table, you should generally prefer to use the regular table operations as opposed to this
 * factory, because they are capable of performing some operations incrementally. However, for small tables this might
 * prove to require less development effort.
 */
public class FunctionGeneratedTableFactory {
    private final Function.Nullary<Table> tableGenerator;
    private final int refreshIntervalMs;
    private long nextRefresh;
    private final Map<String, WritableSource<?>> writableSources = new LinkedHashMap<>();
    private final Map<String, ColumnSource<?>> columns = new LinkedHashMap<>();

    private final TrackingMutableRowSet rowSet;

    /**
     * Create a table that refreshes based on the value of your function, automatically called every refreshIntervalMs.
     *
     * @param tableGenerator a function returning a table to copy into the output table
     * @return a ticking table (assuming sourceTables have been specified) generated by tableGenerator
     */
    public static Table create(Function.Nullary<Table> tableGenerator, int refreshIntervalMs) {
        return new FunctionGeneratedTableFactory(tableGenerator, refreshIntervalMs).getTable();
    }

    /**
     * Create a table that refreshes based on the value of your function, automatically called when any of the
     * sourceTables tick.
     *
     * @param tableGenerator a function returning a table to copy into the output table
     * @param sourceTables The query engine does not know the details of your function inputs. If you are dependent on a
     *        ticking table tables in your tableGenerator function, you can add it to this list so that the function
     *        will be recomputed on each tick.
     * @return a ticking table (assuming sourceTables have been specified) generated by tableGenerator
     */
    public static Table create(Function.Nullary<Table> tableGenerator, Table... sourceTables) {
        final FunctionGeneratedTableFactory factory = new FunctionGeneratedTableFactory(tableGenerator, 0);

        final FunctionBackedTable result = factory.getTable();

        for (Table source : sourceTables) {
            source.listenForUpdates(new BaseTable.ListenerImpl("FunctionGeneratedTable", source, result) {
                @Override
                public void onUpdate(final Update upstream) {
                    result.doRefresh();
                }
            });
        }

        return result;
    }

    private FunctionGeneratedTableFactory(final Function.Nullary<Table> tableGenerator, final int refreshIntervalMs) {
        this.tableGenerator = tableGenerator;
        this.refreshIntervalMs = refreshIntervalMs;
        nextRefresh = System.currentTimeMillis() + this.refreshIntervalMs;

        Table initialTable = tableGenerator.call();
        for (Map.Entry<String, ? extends ColumnSource<?>> entry : initialTable.getColumnSourceMap().entrySet()) {
            ColumnSource<?> columnSource = entry.getValue();
            final ArrayBackedColumnSource<?> memoryColumnSource = ArrayBackedColumnSource.getMemoryColumnSource(
                    0, columnSource.getType(), columnSource.getComponentType());
            columns.put(entry.getKey(), memoryColumnSource);
            writableSources.put(entry.getKey(), memoryColumnSource);
        }

        copyTable(initialTable);

        // enable prev tracking after columns are initialized
        columns.values().forEach(ColumnSource::startTrackingPrevValues);

        rowSet = RowSetFactory.flat(initialTable.size()).toTracking();
    }

    private FunctionBackedTable getTable() {
        return new FunctionBackedTable(rowSet, columns);
    }

    private long updateTable() {
        Table newTable = tableGenerator.call();

        copyTable(newTable);

        return newTable.size();
    }

    private void copyTable(Table source) {
        Map<String, ? extends ColumnSource<?>> sourceColumns = source.getColumnSourceMap();

        RowSet sourceRowSet = source.getRowSet();

        for (Map.Entry<String, ? extends ColumnSource<?>> entry : sourceColumns.entrySet()) {
            WritableSource<?> destColumn = writableSources.get(entry.getKey());
            destColumn.ensureCapacity(sourceRowSet.size());

            long position = 0;
            for (RowSet.Iterator sourceIt = sourceRowSet.iterator(); sourceIt.hasNext();) {
                long current = sourceIt.nextLong();
                // noinspection unchecked
                destColumn.copy((ColumnSource) entry.getValue(), current, position++);
            }
        }
    }

    private class FunctionBackedTable extends QueryTable implements LiveTable {
        FunctionBackedTable(TrackingRowSet rowSet, Map<String, ColumnSource<?>> columns) {
            super(rowSet, columns);
            if (refreshIntervalMs >= 0) {
                setRefreshing(true);
                if (refreshIntervalMs > 0) {
                    UpdateGraphProcessor.DEFAULT.addTable(this);
                }
            }
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() < nextRefresh) {
                return;
            }
            nextRefresh = System.currentTimeMillis() + refreshIntervalMs;

            doRefresh();
        }

        protected void doRefresh() {
            long size = rowSet.size();

            long newSize = updateTable();

            if (newSize < size) {
                final RowSet removed = RowSetFactory.fromRange(newSize, size - 1);
                rowSet.remove(removed);
                final RowSet modified = rowSet.copy();
                notifyListeners(RowSetFactory.empty(), removed, modified);
                return;
            }
            if (newSize > size) {
                final RowSet added = RowSetFactory.fromRange(size, newSize - 1);
                final RowSet modified = rowSet.copy();
                rowSet.insert(added);
                notifyListeners(added, RowSetFactory.empty(), modified);
                return;
            }
            if (size > 0) {
                // no size change, just modified
                final RowSet modified = rowSet.copy();
                notifyListeners(RowSetFactory.empty(), RowSetFactory.empty(), modified);
            }
        }

        @Override
        public void destroy() {
            super.destroy();
            if (refreshIntervalMs > 0) {
                UpdateGraphProcessor.DEFAULT.removeTable(this);
            }
        }
    }
}
