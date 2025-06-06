//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.benchmarking.impl;

import io.deephaven.engine.table.Table;
import io.deephaven.engine.testutil.QueryTableTestBase;
import io.deephaven.benchmarking.BenchmarkTable;
import io.deephaven.benchmarking.BenchmarkTableBuilder;
import io.deephaven.benchmarking.BenchmarkTools;

import static io.deephaven.engine.testutil.TstUtils.assertTableEquals;

public class TestTableGeneration extends QueryTableTestBase {

    public void testCreateHistorical() {
        final PersistentBenchmarkTableBuilder builder = BenchmarkTools.persistentTableBuilder("Carlos", 2000);
        final BenchmarkTable bt = builder.setSeed(0xDEADBEEF)
                .addColumn(BenchmarkTools.stringCol("Stringy", 1, 10))
                .addColumn(BenchmarkTools.numberCol("C2", int.class))
                .addColumn(BenchmarkTools.numberCol("C3", double.class))
                .addColumn(BenchmarkTools.stringCol("C4", 10, 5, 7, 0xFEEDBEEF))
                .addColumn(BenchmarkTools.stringCol("Thingy", 30, 6, 6, 0xB00FB00F))
                .addGroupingColumns("Thingy")
                .setPartitioningFormula("${autobalance_single}")
                .setPartitionCount(10)
                .build();

        final Table historicalTable = bt.getTable();
        Table selected = historicalTable.select();

        // Make sure it gets recorded properly
        assertEquals(2000, selected.size());

        // Make sure we can generate more
        bt.cleanup();


        // Next make sure it's repeatable
        bt.reset();

        assertTableEquals(bt.getTable(), historicalTable);
    }

    public void testCreateIntraday() {
        final BenchmarkTableBuilder builder = BenchmarkTools.persistentTableBuilder("Carlos", 2000);
        final BenchmarkTable bt = builder.setSeed(0xDEADBEEF)
                .addColumn(BenchmarkTools.stringCol("Stringy", 1, 10))
                .addColumn(BenchmarkTools.numberCol("C2", int.class))
                .addColumn(BenchmarkTools.numberCol("C3", double.class))
                .addColumn(BenchmarkTools.stringCol("C4", 10, 5, 7, 0xFEEDBEEF))
                .addColumn(BenchmarkTools.stringCol("Thingy", 30, 6, 6, 0xB00FB00F))
                .build();

        final Table intradayTable = bt.getTable();

        // Make sure it gets recorded properly
        assertEquals(2000, intradayTable.size());

        // Make sure we can generate more
        bt.cleanup();

        // Next make sure it's repeatable
        bt.reset();

        assertTableEquals(bt.getTable(), intradayTable);
    }

    public void testCreateSparseInMemory() {
        final BenchmarkTableBuilder builder = BenchmarkTools.inMemoryTableBuilder("Carlos", 200000);
        final BenchmarkTable bt = builder.setSeed(0xDEADBEEF)
                .addColumn(BenchmarkTools.stringCol("Stringy", 1, 10))
                .addColumn(BenchmarkTools.numberCol("C2", int.class))
                .addColumn(BenchmarkTools.numberCol("C3", double.class))
                .addColumn(BenchmarkTools.stringCol("C4", 10, 5, 7, 0xFEEDBEEF))
                .addColumn(BenchmarkTools.stringCol("Thingy", 30, 6, 6, 0xB00FB00F))
                .build();

        final Table resultTable = BenchmarkTools.applySparsity(bt.getTable(), 2000, 1, 0);

        assertEquals(2000, resultTable.size());
        assertTrue(resultTable.getRowSet().lastRowKey() > 100000);
        // Make sure we can generate more
        bt.cleanup();
    }
}
