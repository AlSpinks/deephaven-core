//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl;

import io.deephaven.engine.context.ExecutionContext;
import io.deephaven.engine.exceptions.TableAlreadyFailedException;
import io.deephaven.engine.table.Table;
import io.deephaven.engine.testutil.ControlledUpdateGraph;
import io.deephaven.engine.testutil.TstUtils;
import io.deephaven.engine.testutil.testcase.RefreshingTableTestCase;
import io.deephaven.engine.util.TableTools;
import io.deephaven.engine.table.impl.select.FormulaEvaluationException;
import io.deephaven.engine.rowset.RowSet;
import junit.framework.TestCase;

import java.util.List;

import static io.deephaven.engine.testutil.TstUtils.assertTableEquals;
import static io.deephaven.engine.testutil.TstUtils.i;
import static io.deephaven.engine.util.TableTools.col;

public class TestListenerFailure extends RefreshingTableTestCase {
    public void testListenerFailure() {
        final QueryTable source = TstUtils.testRefreshingTable(col("Str", "A", "B"));
        final Table updated = ExecutionContext.getContext().getUpdateGraph().sharedLock().computeLocked(
                () -> source.update("UC=Str.toUpperCase()"));

        TableTools.showWithRowSet(updated);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.addToTable(source, i(2, 3), col("Str", "C", "D"));
            source.notifyListeners(i(2, 3), i(), i());
        });

        assertFalse(updated.isFailed());

        allowingError(() -> {
            updateGraph.runWithinUnitTestCycle(() -> {
                TstUtils.addToTable(source, i(4, 5), col("Str", "E", null));
                source.notifyListeners(i(4, 5), i(), i());
            });
            return null;
        }, TestListenerFailure::isNpe);

        assertTrue(updated.isFailed());

        try {
            updated.addUpdateListener(new ErrorListener(updated));
            TestCase.fail("Should not be allowed to listen to failed table");
        } catch (TableAlreadyFailedException tafe) {
            assertEquals("Can not listen to failed table QueryTable", tafe.getMessage());
        }

        try {
            updated.addUpdateListener(
                    new ShiftObliviousInstrumentedListenerAdapter("Dummy", updated, false) {
                        @Override
                        public void onUpdate(RowSet added, RowSet removed, RowSet modified) {}
                    }, false);
            TestCase.fail("Should not be allowed to listen to failed table");
        } catch (TableAlreadyFailedException tafe) {
            assertEquals("Can not listen to failed table QueryTable", tafe.getMessage());
        }
    }

    private static boolean isNpe(List<Throwable> throwables) {
        if (1 != throwables.size()) {
            return false;
        }
        if (!throwables.get(0).getClass().equals(FormulaEvaluationException.class)) {
            return false;
        }
        if (!throwables.get(0).getMessage().equals("In formula: UC = Str.toUpperCase()")) {
            return false;
        }
        return throwables.get(0).getCause().getClass().equals(NullPointerException.class);
    }

    public void testMemoCheck() {
        QueryTable.setMemoizeResults(true);

        final QueryTable source = TstUtils.testRefreshingTable(col("Str", "A", "B"));
        final QueryTable viewed = (QueryTable) source.updateView("UC=Str.toUpperCase()");
        final Table filtered = viewed.where("UC=`A`");

        TableTools.showWithRowSet(filtered);

        final ControlledUpdateGraph updateGraph = ExecutionContext.getContext().getUpdateGraph().cast();
        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.addToTable(source, i(2, 3), col("Str", "C", "D"));
            source.notifyListeners(i(2, 3), i(), i());
        });

        assertFalse(filtered.isFailed());

        final Table filteredAgain = viewed.where("UC=`A`");
        assertSame(filtered, filteredAgain);

        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.addToTable(source, i(4, 5), col("Str", "E", null));
            source.notifyListeners(i(4, 5), i(), i());
        });

        assertTrue(filtered.isFailed());
        assertTrue(filteredAgain.isFailed());

        updateGraph.runWithinUnitTestCycle(() -> {
            TstUtils.removeRows(source, i(5));
            source.notifyListeners(i(), i(5), i());
        });

        final Table filteredYetAgain = viewed.where("UC=`A`");
        assertNotSame(filtered, filteredYetAgain);
        assertFalse(filteredYetAgain.isFailed());
        assertTableEquals(TableTools.newTable(col("Str", "A"), col("UC", "A")), filteredYetAgain);
    }
}
