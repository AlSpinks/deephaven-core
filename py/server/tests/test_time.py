#
# Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
#

import unittest
from time import sleep
from datetime import datetime

from deephaven.constants import NULL_LONG, NULL_INT
from deephaven.time import *
from tests.testbase import BaseTestCase


class TimeTestCase(BaseTestCase):

    # region Constants

    def test_constants(self):
        self.assertEqual(1000,MICRO)
        self.assertEqual(1000000,MILLI)
        self.assertEqual(1000000000,SECOND)
        self.assertEqual(60*1000000000,MINUTE)
        self.assertEqual(60*60*1000000000,HOUR)
        self.assertEqual(24*60*60*1000000000,DAY)
        self.assertEqual(7*24*60*60*1000000000,WEEK)
        self.assertEqual(365*24*60*60*1000000000,YEAR)

        self.assertEqual(1/SECOND, SECONDS_PER_NANO)
        self.assertEqual(1/MINUTE, MINUTES_PER_NANO)
        self.assertEqual(1/HOUR, HOURS_PER_NANO)
        self.assertEqual(1/DAY, DAYS_PER_NANO)
        self.assertEqual(1/YEAR, YEARS_PER_NANO)

    #TODO:  DateStyle

    # endregion

    # region: CLock

    def test_now(self):
        for system in [True, False]:
            for resolution in ['ns', 'ms']:
                dt = now(system=system, resolution=resolution)
                sleep(1)
                dt1 = now(system=system, resolution=resolution)
                self.assertGreaterEqual(diff_nanos(dt, dt1), 100000000)

    def test_today(self):
        tz = time_zone("America/New_york")
        td = today(tz)
        target = datetime.today().strftime('%Y-%m-%d')
        self.assertEqual(td, target)

    # endregion
    
    # region: Time Zone

    def test_time_zone:
        tz = time_zone("America/New_York")
        self.assertEqual(str(tz), "America/New_York")

        tz = time_zone("MN")
        self.assertEqual(str(tz), "America/Chicago")

        tz = time_zone(None)
        self.assertEqual(str(tz), "America/New_York")

    # endregion
    
    # region: Conversions: Time Units

    #TODO:  micros_to_nanos
    #TODO:  millis_to_nanos
    #TODO:  seconds_to_nanos
    #TODO:  nanos_to_micros
    #TODO:  millis_to_micros
    #TODO:  seconds_to_micros
    #TODO:  nanos_to_millis
    #TODO:  micros_to_millis
    #TODO:  seconds_to_millis
    #TODO:  nanos_to_seconds
    #TODO:  micros_to_seconds
    #TODO:  millis_to_seconds

    # endregion
    
    # region: Conversions: Date Time Types

    #TODO:  to_instant
    #TODO:  to_zdt
    #TODO:  make_instant
    #TODO:  make_zdt
    #TODO:  to_local_date
    #TODO:  to_local_time

    # endregion
    
    # region: Conversions: Epoch

    #TODO:  epoch_nanos
    #TODO:  epoch_micros
    #TODO:  epoch_millis
    #TODO:  epoch_seconds
    #TODO:  epoch_nanos_to_instant
    #TODO:  epoch_micros_to_instant
    #TODO:  epoch_millis_to_instant
    #TODO:  epoch_seconds_to_instant
    #TODO:  epoch_nanos_to_zdt
    #TODO:  epoch_micros_to_zdt
    #TODO:  epoch_millis_to_zdt
    #TODO:  epoch_seconds_to_zdt
    #TODO:  epoch_auto_to_epoch_nanos
    #TODO:  epoch_auto_to_instant
    #TODO:  epoch_auto_to_zdt

    # endregion
    
    # region: Conversions: Excel

    #TODO:  to_excel_time
    #TODO:  excel_to_instant
    #TODO:  excel_to_zdt

    # endregion
    
    # region: Arithmetic

    #TODO:  plus_period
    #TODO:  minus_period
    #TODO:  diff_nanos
    #TODO:  diff_micros
    #TODO:  diff_millis
    #TODO:  diff_seconds
    #TODO:  diff_minutes
    #TODO:  diff_days
    #TODO:  diff_years

    # endregion

    # region: Comparisons

    #TODO:  is_before
    #TODO:  is_before_or_equal
    #TODO:  is_after
    #TODO:  is_after_or_equal

    # endregion

    # region: Chronology

    #TODO:  nanos_of_milli
    #TODO:  micros_of_milli
    #TODO:  nanos_of_second
    #TODO:  micros_of_second
    #TODO:  millis_of_second
    #TODO:  second_of_minute
    #TODO:  minute_of_hour
    #TODO:  nanos_of_day
    #TODO:  millis_of_day
    #TODO:  second_of_day
    #TODO:  minute_of_day
    #TODO:  hour_of_day
    #TODO:  day_of_week
    #TODO:  day_of_month
    #TODO:  day_of_year
    #TODO:  month_of_year
    #TODO:  year
    #TODO:  year_of_century
    #TODO:  at_midnight

    # endregion

    # region: Binning

    def test_lower_bin(self):
        dt = now()
        self.assertGreaterEqual(diff_nanos(lower_bin(dt, 1000000, MINUTE), dt), 0)

    def test_upper_bin(self):
        dt = now()
        self.assertGreaterEqual(diff_nanos(dt, upper_bin(dt, 1000000, MINUTE)), 0)

    # endregion
    
    # region: Format

    #TODO:  format_nanos
    #TODO:  format_datetime
    #TODO:  format_date

    # endregion
    
    # region: Parse

    def test_parse_time_zone:
        tz = parse_time_zone("America/New_York")
        self.assertEqual(str(tz), "America/New_York")

        tz = parse_time_zone("MN")
        self.assertEqual(str(tz), "America/Chicago")

        tz = parse_time_zone(None)
        self.assertEqual(str(tz), "America/New_York")

    def test_parse_nanos(self):
        time_str = "530000:59:39.123456789"
        in_nanos = parse_nanos(time_str)
        self.assertEqual(str(in_nanos), "1908003579123456789")

        with self.assertRaises(DHError) as cm:
            time_str = "530000:59:39.X"
            in_nanos = parse_nanos(time_str)
        self.assertIn("RuntimeException", str(cm.exception))

        time_str = "00:59:39.X"
        in_nanos = parse_nanos(time_str, quiet=True)
        self.assertEqual(in_nanos, NULL_LONG)

        time_str = "1:02:03"
        in_nanos = parse_nanos(time_str)
        time_str2 = format_nanos(in_nanos)
        self.assertEqual(time_str2, time_str)

    def test_parse_period(self):
        period_str = "P1W"
        period = parse_period(period_str)
        self.assertEqual(str(period).upper(), period_str)

        period_str = "P1M"
        period = parse_period(period_str)
        self.assertEqual(str(period).upper(), period_str)

        with self.assertRaises(DHError) as cm:
            period_str = "PT1Y"
            period = parse_period(period_str)
        self.assertIn("RuntimeException", str(cm.exception))

        period = parse_period(period_str, quiet=True)
        self.assertNone(period)

    def test_parse_duration(self):
        duration_str = "PT1M"
        duration = parse_duration(duration_str)
        self.assertEqual(str(duration).upper(), duration_str)

        duration_str = "PT1H"
        duration = parse_duration(duration_str)
        self.assertEqual(str(duration).upper(), duration_str)

        with self.assertRaises(DHError) as cm:
            duration = parse_duration("T1Q")
        self.assertIn("RuntimeException", str(cm.exception))

        duration = parse_duration("T1Q", quiet=True)
        self.assertNone(duration)

    def test_parse_instant(self):
        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "NY"
        dt = parse_instant(f"{datetime_str} {timezone_str}")
        self.assertTrue(str(dt).startswith(datetime_str))

        with self.assertRaises(DHError) as cm:
            datetime_str = "2021-12-10T23:59:59"
            timezone_str = "--"
            dt = parse_instant(f"{datetime_str} {timezone_str}")
        self.assertIn("RuntimeException", str(cm.exception))

        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "--"
        dt = parse_instant(f"{datetime_str} {timezone_str}", quiet=True)
        self.assertNone(dt)

    def test_parse_zdt(self):
        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "NY"
        dt = parse_zdt(f"{datetime_str} {timezone_str}")
        self.assertTrue(str(dt).startswith(datetime_str))

        with self.assertRaises(DHError) as cm:
            datetime_str = "2021-12-10T23:59:59"
            timezone_str = "--"
            dt = parse_zdt(f"{datetime_str} {timezone_str}")
        self.assertIn("RuntimeException", str(cm.exception))

        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "--"
        dt = parse_zdt(f"{datetime_str} {timezone_str}", quiet=True)
        self.assertNone(dt)

    def test_parse_time_precision(self):
        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "NY"
        tp = parse_time_precision(f"{datetime_str} {timezone_str}")
        self.assertEqual(tp, "SecondOfMinute")

        with self.assertRaises(DHError) as cm:
            datetime_str = "2021-12-10T23:59:59"
            timezone_str = "--"
            tp = parse_time_precision(f"{datetime_str} {timezone_str}")
        self.assertIn("RuntimeException", str(cm.exception))

        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "--"
        tp = parse_time_precision(f"{datetime_str} {timezone_str}", quiet=True)
        self.assertNone(tp)

    def test_parse_local_date(self):
        date_str = "2021-12-10"
        dt = parse_local_date(date_str)
        self.assertTrue(str(dt), date_str)

        with self.assertRaises(DHError) as cm:
            date_str = "2021-x12-10"
            dt = parse_local_date(date_str)
        self.assertIn("RuntimeException", str(cm.exception))

        date_str = "2021-x12-10"
        dt = parse_local_date(date_str, quiet=True)
        self.assertNone(dt)

    def test_parse_local_time(self):
        time_str = "23:59:59"
        dt = parse_local_time(time_str)
        self.assertTrue(str(dt), time_str)

        with self.assertRaises(DHError) as cm:
            time_str = "23:59x:59"
            dt = parse_local_time(time_str)
        self.assertIn("RuntimeException", str(cm.exception))

        time_str = "23:59x:59"
        dt = parse_local_time(time_str, quiet=True)
        self.assertNone(dt)

    # endregion










    def test_datetime_at_midnight(self):
        datetime_str = "2021-12-10T02:59:59"
        timezone_str = "NY"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        mid_night_time_ny = datetime_at_midnight(dt, TimeZone.NY)
        mid_night_time_pt = datetime_at_midnight(dt, TimeZone.PT)
        self.assertEqual(
            diff_nanos(mid_night_time_ny, mid_night_time_pt) // 10 ** 9, -21 * 60 * 60
        )

        # DST ended in NY but not in PT
        datetime_str = "2021-11-08T02:59:59"
        timezone_str = "NY"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        mid_night_time_ny = datetime_at_midnight(dt, TimeZone.NY)
        mid_night_time_pt = datetime_at_midnight(dt, TimeZone.PT)
        self.assertEqual(
            diff_nanos(mid_night_time_ny, mid_night_time_pt) // 10 ** 9, -22 * 60 * 60
        )

    def test_day_of_month(self):
        dt = now()
        self.assertIn(day_of_month(dt, TimeZone.MT), range(1, 32))
        datetime_str = "2021-12-01T00:01:05"
        timezone_str = "HI"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(day_of_month(dt, TimeZone.HI), 1)
        self.assertEqual(day_of_month(None, TimeZone.HI), NULL_INT)

    def test_day_of_week(self):
        dt = now()
        self.assertIn(day_of_week(dt, TimeZone.MT), range(1, 8))
        self.assertEqual(day_of_week(None, TimeZone.MT), NULL_INT)

    def test_day_of_year(self):
        dt = now()
        self.assertIn(day_of_year(dt, TimeZone.MT), range(1, 366))
        self.assertEqual(day_of_year(None, TimeZone.MT), NULL_INT)

    def test_diff_days(self):
        dt1 = now()
        dt2 = plus_nanos(dt1, 2*DAY)
        self.assertGreaterEqual(diff_days(dt2, dt1), 1.9)

    def test_diff_years(self):
        dt1 = now()
        dt2 = plus_nanos(dt1, 2*YEAR)
        self.assertGreaterEqual(diff_years(dt2, dt1), 1.9)

    def test_format_datetime(self):
        dt = now()
        self.assertIn(TimeZone.SYD.name, format_datetime(dt, TimeZone.SYD))

    def test_format_nanos(self):
        dt = now()
        ns = nanos(dt)
        ns_str1 = format_nanos(ns).split(".")[-1]
        ns_str2 = format_datetime(dt, TimeZone.UTC).split(".")[-1]
        self.assertTrue(ns_str2.startswith(ns_str1))

    def test_format_date(self):
        dt = now()
        self.assertEqual(3, len(format_date(dt, TimeZone.MOS).split("-")))

    def test_hour_of_day(self):
        dt = now()
        self.assertIn(hour_of_day(dt, TimeZone.AL), range(0, 24))
        self.assertEqual(hour_of_day(None, TimeZone.AL), NULL_INT)

    def test_is_after(self):
        dt1 = now()
        sleep(0.001)
        dt2 = now()
        self.assertTrue(is_after(dt2, dt1))
        self.assertFalse(is_after(None, dt1))

    def test_is_before(self):
        dt1 = now()
        sleep(0.001)
        dt2 = now()
        self.assertFalse(is_before(dt2, dt1))
        self.assertFalse(is_after(None, dt1))


    def test_millis(self):
        dt = now()
        self.assertGreaterEqual(nanos(dt), millis(dt) * 10 ** 6)
        self.assertEqual(millis(None), NULL_LONG)

    def test_millis_of_second(self):
        dt = now()
        self.assertGreaterEqual(millis_of_second(dt, TimeZone.AT), 0)
        self.assertEqual(millis_of_second(None, TimeZone.AT), NULL_INT)

    def test_millis_to_nanos(self):
        dt = now()
        ms = millis(dt)
        self.assertEqual(ms * 10 ** 6, millis_to_nanos(ms))
        self.assertEqual(NULL_LONG, millis_to_nanos(NULL_LONG))

    def test_minus(self):
        dt1 = now()
        dt2 = now()
        self.assertGreaterEqual(0, minus(dt1, dt2))
        self.assertEqual(NULL_LONG, minus(None, dt2))

    def test_minus_nanos(self):
        dt = now()
        dt1 = minus_nanos(dt, 1)
        self.assertEqual(1, diff_nanos(dt1, dt))

    def test_minus_period(self):
        period_str = "T1H"
        period = to_period(period_str)

        dt = now()
        dt1 = minus_period(dt, period)
        self.assertEqual(diff_nanos(dt1, dt), 60 * 60 * 10 ** 9)

    def test_minute_of_day(self):
        datetime_str = "2021-12-10T00:59:59"
        timezone_str = "BT"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(59, minute_of_day(dt, TimeZone.BT))
        self.assertEqual(NULL_INT, minute_of_day(None, TimeZone.BT))

    def test_minute_of_hour(self):
        datetime_str = "2021-12-10T23:59:59"
        timezone_str = "CE"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(59, minute_of_hour(dt, TimeZone.CE))
        self.assertEqual(NULL_INT, minute_of_hour(None, TimeZone.CE))

    def test_month_of_year(self):
        datetime_str = "2021-08-10T23:59:59"
        timezone_str = "CH"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(8, month_of_year(dt, TimeZone.CH))
        self.assertEqual(NULL_INT, month_of_year(None, TimeZone.CH))

    def test_nanos_of_day(self):
        datetime_str = "2021-12-10T00:00:01"
        timezone_str = "CT"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(10 ** 9, nanos_of_day(dt, TimeZone.CT))
        self.assertEqual(NULL_LONG, nanos_of_day(None, TimeZone.CT))

    def test_nanos_of_second(self):
        datetime_str = "2021-12-10T00:00:01.000000123"
        timezone_str = "ET"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(123, nanos_of_second(dt, TimeZone.ET))
        self.assertEqual(NULL_LONG, nanos_of_second(None, TimeZone.ET))

    def test_nanos_to_millis(self):
        dt = now()
        ns = nanos(dt)
        self.assertEqual(ns // 10 ** 6, nanos_to_millis(ns))
        self.assertEqual(NULL_LONG, nanos_to_millis(NULL_LONG))

    def test_nanos_to_time(self):
        dt = now()
        ns = nanos(dt)
        dt1 = nanos_to_datetime(ns)
        self.assertEqual(dt, dt1)
        self.assertEqual(None, nanos_to_datetime(NULL_LONG))

    def test_plus_period(self):
        period_str = "T1H"
        period = to_period(period_str)

        dt = now()
        dt1 = plus_period(dt, period)
        self.assertEqual(diff_nanos(dt, dt1), 60 * 60 * 10 ** 9)

        period_str = "1WT1H"
        period = to_period(period_str)
        dt2 = plus_period(dt, period)
        self.assertEqual(diff_nanos(dt, dt2), (7 * 24 + 1) * 60 * 60 * 10 ** 9)

    def test_plus_nanos(self):
        dt = now()
        dt1 = plus_nanos(dt, 1)
        self.assertEqual(1, diff_nanos(dt, dt1))
        self.assertEqual(None, plus_nanos(None, 1))

    def test_second_of_day(self):
        datetime_str = "2021-12-10T00:01:05"
        timezone_str = "HI"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(65, second_of_day(dt, TimeZone.HI))
        self.assertEqual(NULL_INT, second_of_day(None, TimeZone.HI))

    def test_second_of_minute(self):
        datetime_str = "2021-12-10T00:01:05"
        timezone_str = "HK"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(5, second_of_minute(dt, TimeZone.HK))
        self.assertEqual(NULL_INT, second_of_minute(None, TimeZone.HK))


    def test_year(self):
        datetime_str = "2021-12-10T00:01:05"
        timezone_str = "IN"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(2021, year(dt, TimeZone.IN))
        self.assertEqual(NULL_INT, year(None, TimeZone.IN))

    def test_year_of_century(self):
        datetime_str = "2021-12-10T00:01:05"
        timezone_str = "JP"
        dt = to_datetime(f"{datetime_str} {timezone_str}")
        self.assertEqual(21, year_of_century(dt, TimeZone.JP))
        self.assertEqual(NULL_INT, year_of_century(None, TimeZone.JP))

    # def test_timezone(self):
    #     default_tz = TimeZone.get_default_timezone()
    #     TimeZone.set_default_timezone(TimeZone.UTC)
    #     tz1 = TimeZone.get_default_timezone()
    #     self.assertEqual(TimeZone.UTC, tz1)
    #     TimeZone.set_default_timezone(TimeZone.JP)
    #     tz2 = TimeZone.get_default_timezone()
    #     self.assertEqual(TimeZone.JP, tz2)
    #     TimeZone.set_default_timezone(default_tz)


if __name__ == "__main__":
    unittest.main()
