//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl;

import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.TableDefinition;
import io.deephaven.engine.table.impl.DeferredViewTable.TableReference;
import io.deephaven.engine.updategraph.UpdateSourceRegistrar;
import io.deephaven.engine.table.impl.locations.TableLocationProvider;
import io.deephaven.engine.table.impl.select.SelectColumn;

/**
 * Simple source table with no partitioning support.
 */
public class SimpleSourceTable extends SourceTable<SimpleSourceTable> {

    /**
     * @param tableDefinition A TableDefinition
     * @param description A human-readable description for this table
     * @param componentFactory A component factory for creating column source managers
     * @param locationProvider A TableLocationProvider, for use in discovering the locations that compose this table
     * @param updateSourceRegistrar Callback for registering live tables for refreshes, null if this table is not live
     */
    public SimpleSourceTable(TableDefinition tableDefinition,
            String description,
            SourceTableComponentFactory componentFactory,
            TableLocationProvider locationProvider,
            UpdateSourceRegistrar updateSourceRegistrar) {
        super(tableDefinition, description, componentFactory, locationProvider, updateSourceRegistrar);
    }

    protected SimpleSourceTable newInstance(TableDefinition tableDefinition,
            String description,
            SourceTableComponentFactory componentFactory,
            TableLocationProvider locationProvider,
            UpdateSourceRegistrar updateSourceRegistrar) {
        return new SimpleSourceTable(tableDefinition, description, componentFactory, locationProvider,
                updateSourceRegistrar);
    }

    @Override
    protected SimpleSourceTable copy() {
        final SimpleSourceTable result = newInstance(definition, getDescription(), componentFactory, locationProvider,
                updateSourceRegistrar);
        LiveAttributeMap.copyAttributes(this, result, ak -> true);
        return result;
    }

    @Override
    protected final SourceTable<?> redefine(TableDefinition newDefinition) {
        if (newDefinition.getColumnNames().equals(definition.getColumnNames())) {
            // Nothing changed - we have the same columns in the same order.
            return this;
        }
        return newInstance(newDefinition, getDescription() + "-retainColumns", componentFactory, locationProvider,
                updateSourceRegistrar);
    }

    @Override
    protected final Table redefine(TableDefinition newDefinitionExternal, TableDefinition newDefinitionInternal,
            SelectColumn[] viewColumns) {
        return new DeferredViewTable(newDefinitionExternal, getDescription() + "-redefined",
                new TableReference(redefine(newDefinitionInternal)), new String[0], viewColumns, null);
    }
}
