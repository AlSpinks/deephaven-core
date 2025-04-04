//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.plot.datasets.xy;

import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.plot.AxesImpl;
import io.deephaven.plot.TableSnapshotSeries;
import io.deephaven.plot.datasets.data.IndexableNumericDataSwappableTable;
import io.deephaven.plot.errors.PlotInfo;
import io.deephaven.plot.util.ArgumentValidations;
import io.deephaven.plot.util.tables.SwappableTable;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.context.QueryScope;

import java.util.function.Function;

public class XYDataSeriesSwappableTableArray extends XYDataSeriesArray implements TableSnapshotSeries {
    private static final long serialVersionUID = 1L;

    private final SwappableTable swappableTable;
    private final String x;
    private final String y;
    private Table localTable;

    public XYDataSeriesSwappableTableArray(final AxesImpl axes, final int id, final Comparable name,
            final SwappableTable swappableTable, final String x, final String y) {
        super(axes, id, name, new IndexableNumericDataSwappableTable(swappableTable, x, new PlotInfo(axes, name)),
                new IndexableNumericDataSwappableTable(swappableTable, y, new PlotInfo(axes, name)));

        this.swappableTable = swappableTable;
        this.x = x;
        this.y = y;
    }

    @Override
    public XYDataSeriesArray copy(AxesImpl axes) {
        return new XYDataSeriesSwappableTableArray(this, axes);
    }

    private XYDataSeriesSwappableTableArray(final XYDataSeriesSwappableTableArray series, final AxesImpl axes) {
        super(series, axes);
        this.swappableTable = series.swappableTable;
        this.x = series.x;
        this.y = series.y;
    }

    private <S, T> Function<Table, Table> constructPartitionedTableFromFunction(final Function<S, T> function,
            final Class resultClass, final String onColumn, final String columnName) {
        ArgumentValidations.assertNotNull(function, "function", getPlotInfo());
        final String queryFunction = columnName + "Function";
        return t -> {
            QueryScope.addParam(queryFunction, function);
            ExecutionContext.getContext().getQueryLibrary().importClass(resultClass);
            return t.update(columnName + " = (" + resultClass.getSimpleName() + ") " + queryFunction + ".apply("
                    + onColumn + ")");
        };
    }
}
