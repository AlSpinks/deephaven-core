#
#   Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
#
import unittest

from deephaven2 import read_csv
from deephaven2.plot import Figure
from tests.testbase import BaseTestCase


class FigureTestCase(BaseTestCase):
    def setUp(self):
        self.test_table = read_csv("tests/data/test_table.csv")

    def tearDown(self) -> None:
        self.test_table = None

    def test_figure(self):
        figure = Figure()
        new_f = figure.plot_xy("plot1", self.test_table, x="a", y="b")
        plot1 = new_f.show()
        new_f = figure.plot_xy("plot2", x=[1, 2, 3], y=[1.0, 2.0, 3.0])
        plot2 = new_f.show()
        self.assertIsNotNone(new_f)


if __name__ == '__main__':
    unittest.main()
