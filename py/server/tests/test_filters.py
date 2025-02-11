#
# Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
#

import unittest

from deephaven import new_table, read_csv, DHError
from deephaven.column import string_col
from deephaven.filters import Filter, PatternMode, and_, is_not_null, is_null, or_, not_, pattern
from tests.testbase import BaseTestCase


class FilterTestCase(BaseTestCase):
    def setUp(self):
        super().setUp()
        self.test_table = read_csv("tests/data/test_table.csv")

    def tearDown(self) -> None:
        self.test_table = None
        super().tearDown()

    def test_pattern_filter(self):
        new_test_table = self.test_table.update("X = String.valueOf(d)")
        regex_filter = pattern(PatternMode.MATCHES, "X", "(?s)...")
        with self.assertRaises(DHError):
            filtered_table = self.test_table.where(filters=regex_filter)

        filtered_table = new_test_table.where(filters=regex_filter)
        self.assertLessEqual(filtered_table.size, new_test_table.size)

        new_test_table = new_test_table.update("Y = String.valueOf(e)")
        regex_filter1 = pattern(PatternMode.MATCHES, "Y", "(?s).0.")
        filtered_table = new_test_table.where(filters=[regex_filter, regex_filter1])
        self.assertLessEqual(filtered_table.size, new_test_table.size)

    def test_filter(self):
        conditions = ["a > 100", "b < 1000", "c < 0"]
        filters = Filter.from_(conditions)
        filtered_table = self.test_table.where(filters)
        filter_and = and_(filters)
        filtered_table_and = self.test_table.where(filter_and)
        self.assert_table_equals(filtered_table, filtered_table_and)

        filter_or = or_(filters)
        filtered_table_or = self.test_table.where(filter_or)
        self.assertGreater(filtered_table_or.size, filtered_table_and.size)

        filter_not = not_(filter_or)
        filtered_table_not = self.test_table.where(filter_not)
        self.assertEqual(filtered_table_or.size + filtered_table_not.size, self.test_table.size)

        filtered_table_mixed = self.test_table.where(
            ["a > 100", "b < 1000", Filter.from_("c < 0")]
        )
        self.assert_table_equals(filtered_table_mixed, filtered_table_and)

    def test_is_null(self):
        x = new_table([string_col("X", ["a", "b", "c", None, "e", "f"])])
        x_is_null = new_table([string_col("X", [None])])
        x_not_is_null = new_table([string_col("X", ["a", "b", "c", "e", "f"])])
        self.assert_table_equals(x.where(is_null("X")), x_is_null)
        self.assert_table_equals(x.where(not_(is_null("X"))), x_not_is_null)

    def test_is_not_null(self):
        x = new_table([string_col("X", ["a", "b", "c", None, "e", "f"])])
        x_is_not_null = new_table([string_col("X", ["a", "b", "c", "e", "f"])])
        x_not_is_not_null = new_table([string_col("X", [None])])
        self.assert_table_equals(x.where(is_not_null("X")), x_is_not_null)
        self.assert_table_equals(x.where(not_(is_not_null("X"))), x_not_is_not_null)

if __name__ == '__main__':
    unittest.main()
