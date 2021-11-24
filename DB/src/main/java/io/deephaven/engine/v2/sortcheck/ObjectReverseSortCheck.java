/* ---------------------------------------------------------------------------------------------------------------------
 * AUTO-GENERATED CLASS - DO NOT EDIT MANUALLY - for any changes edit CharSortCheck and regenerate
 * ------------------------------------------------------------------------------------------------------------------ */
package io.deephaven.engine.v2.sortcheck;

import io.deephaven.engine.chunk.Attributes;
import io.deephaven.engine.chunk.ObjectChunk;
import io.deephaven.engine.chunk.Chunk;

public class ObjectReverseSortCheck implements SortCheck {
    static final SortCheck INSTANCE = new ObjectReverseSortCheck();

    @Override
    public int sortCheck(Chunk<? extends Attributes.Values> valuesToCheck) {
        return sortCheck(valuesToCheck.asObjectChunk());
    }

    private int sortCheck(ObjectChunk<?, ? extends Attributes.Values> valuesToCheck) {
        if (valuesToCheck.size() == 0) {
            return -1;
        }
        Object last = valuesToCheck.get(0);
        for (int ii = 1; ii < valuesToCheck.size(); ++ii) {
            final Object current = valuesToCheck.get(ii);
            if (!leq(last, current)) {
                return ii - 1;
            }
            last = current;
        }
        return -1;
    }

    // region comparison functions
    // descending comparison
    private static int doComparison(Object lhs, Object rhs) {
        if (lhs == rhs) {
            return 0;
        }
        if (lhs == null) {
            return 1;
        }
        if (rhs == null) {
            return -1;
        }
        //noinspection unchecked
        return ((Comparable)rhs).compareTo(lhs);
    }
    // endregion comparison functions

    private static boolean leq(Object lhs, Object rhs) {
        return doComparison(lhs, rhs) <= 0;
    }
}
