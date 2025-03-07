//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.util;

import io.deephaven.engine.table.Table;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.time.DateTimeUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public class HtmlTable {
    @NotNull
    public static String html(Table source) {
        List<String> columnNames = source.getDefinition().getColumnNames();
        String[] columns = columnNames.toArray(new String[columnNames.size()]);
        StringBuilder out = new StringBuilder();
        out.append("<table border=\"1\">\n");

        out.append("<tr>\n");
        for (String column : columns) {
            out.append("<th>").append(column).append("</th>\n");
        }
        out.append("</tr>\n");

        final Collection<? extends ColumnSource> columnSources = source.getColumnSources();
        for (final RowSet.Iterator ii = source.getRowSet().iterator(); ii.hasNext();) {
            out.append("<tr>");
            final long key = ii.nextLong();
            for (ColumnSource columnSource : columnSources) {
                out.append("<td>");
                final Object value = columnSource.get(key);
                if (value instanceof String) {
                    out.append(StringEscapeUtils.escapeCsv((String) value));
                } else if (value instanceof Instant) {
                    out.append(DateTimeUtils.formatDateTime((Instant) value, DateTimeUtils.timeZone()));
                } else {
                    out.append(TableTools.nullToNullString(value));
                }
                out.append("</td>");
            }
            out.append("</tr>\n");
        }
        out.append("</table>\n");
        return out.toString();
    }

}
