/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.tables.utils;

import io.deephaven.base.CompareUtils;
import io.deephaven.base.clock.TimeZones;
import io.deephaven.base.testing.BaseArrayTestCase;
import io.deephaven.util.DateUtil;
import io.deephaven.util.QueryConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class TestDateTimeUtils extends BaseArrayTestCase {

    public void testMillis() throws Exception {
        org.joda.time.DateTime jodaDateTime = new org.joda.time.DateTime("2010-01-01T12:13:14.999");

        DateTime dateTime = new DateTime(jodaDateTime.getMillis() * 1000000 + 123456);

        assertEquals(jodaDateTime.getMillis(), DateTimeUtils.millis(dateTime));

        assertEquals(io.deephaven.util.QueryConstants.NULL_LONG, DateTimeUtils.millis(null));
    }

    public void testNanos() throws Exception {
        org.joda.time.DateTime jodaDateTime = new org.joda.time.DateTime("2010-01-01T12:13:14.999");

        DateTime dateTime = new DateTime(jodaDateTime.getMillis() * 1000000 + 123456);

        assertEquals(jodaDateTime.getMillis() * 1000000 + 123456, DateTimeUtils.nanos(dateTime));

        assertEquals(io.deephaven.util.QueryConstants.NULL_LONG, DateTimeUtils.nanos((DateTime) null));
    }

    public void testMidnightConversion() throws Exception {
        org.joda.time.DateTime jodaDateTime = new org.joda.time.DateTime("2010-01-01T12:13:14.999");
        org.joda.time.DateTime jodaMidnight = new org.joda.time.DateTime("2010-01-01T00:00:00.000-05");

        DateTime dateTime = new DateTime(jodaDateTime.getMillis() * 1000000 + 123456);
        DateTime midnight = DateTimeUtils.dateAtMidnight(dateTime, TimeZone.TZ_NY);

        assertEquals(jodaMidnight.getMillis(), DateTimeUtils.millis(midnight));
        assertEquals(jodaMidnight.getMillis(),
                DateTimeUtils.millisToDateAtMidnightNy(dateTime.getMillis()).getMillis());

        assertNull(DateTimeUtils.millisToDateAtMidnightNy(io.deephaven.util.QueryConstants.NULL_LONG));
    }

    public void testIsBefore() throws Exception {
        org.joda.time.DateTime jodaDateTime1 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");
        org.joda.time.DateTime jodaDateTime2 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");

        DateTime dateTime1 = new DateTime(jodaDateTime1.getMillis() * 1000000 + 123456);
        DateTime dateTime2 = new DateTime(jodaDateTime2.getMillis() * 1000000 + 123457);

        assertTrue(DateTimeUtils.isBefore(dateTime1, dateTime2));
        assertFalse(DateTimeUtils.isBefore(dateTime2, dateTime1));
        assertFalse(DateTimeUtils.isBefore(null, dateTime2));
        assertFalse(DateTimeUtils.isBefore(null, null));
        assertFalse(DateTimeUtils.isBefore(dateTime1, null));
    }

    public void testIsAfter() throws Exception {
        org.joda.time.DateTime jodaDateTime1 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");
        org.joda.time.DateTime jodaDateTime2 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");

        DateTime dateTime1 = new DateTime(jodaDateTime1.getMillis() * 1000000 + 123456);
        DateTime dateTime2 = new DateTime(jodaDateTime2.getMillis() * 1000000 + 123457);

        assertFalse(DateTimeUtils.isAfter(dateTime1, dateTime2));
        assertTrue(DateTimeUtils.isAfter(dateTime2, dateTime1));
        assertFalse(DateTimeUtils.isAfter(null, dateTime2));
        assertFalse(DateTimeUtils.isAfter(null, null));
        assertFalse(DateTimeUtils.isAfter(dateTime1, null));
    }

    public void testPlus() throws Exception {
        org.joda.time.DateTime jodaDateTime = new org.joda.time.DateTime("2010-01-01T12:13:14.999");

        DateTime dateTime = new DateTime(jodaDateTime.getMillis() * 1000000 + 123456);

        Period period = new Period("T1h");

        assertEquals(dateTime.getNanos() + 3600000000000L, DateTimeUtils.plus(dateTime, period).getNanos());

        period = new Period("-T1h");

        assertEquals(dateTime.getNanos() - 3600000000000L, DateTimeUtils.plus(dateTime, period).getNanos());


        // overflow plus
        DateTimeUtils.plus(new DateTime(Long.MAX_VALUE - 10), 10); // edge at max
        try {
            DateTimeUtils.plus(new DateTime(Long.MAX_VALUE), 1);
            fail("This should have overflowed");
        } catch (DateTimeUtils.DateTimeOverflowException e) {
            // ok
        }

        DateTimeUtils.plus(new DateTime(Long.MIN_VALUE + 10), -10); // edge at min
        try {
            DateTimeUtils.plus(new DateTime(Long.MIN_VALUE), -1);
            fail("This should have overflowed");
        } catch (DateTimeUtils.DateTimeOverflowException e) {
            // ok
        }
    }

    public void testMinus() throws Exception {
        org.joda.time.DateTime jodaDateTime1 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");
        org.joda.time.DateTime jodaDateTime2 = new org.joda.time.DateTime("2010-01-01T13:13:14.999");

        DateTime dateTime1 = new DateTime(jodaDateTime1.getMillis() * 1000000 + 123456);
        DateTime dateTime2 = new DateTime(jodaDateTime2.getMillis() * 1000000 + 123456);

        assertEquals(-3600000000000L, DateTimeUtils.minus(dateTime1, dateTime2));
        assertEquals(3600000000000L, DateTimeUtils.minus(dateTime2, dateTime1));

        Period period = new Period("T1h");

        assertEquals(dateTime1.getNanos() - 3600000000000L, DateTimeUtils.minus(dateTime1, period).getNanos());

        period = new Period("-T1h");

        assertEquals(dateTime1.getNanos() + 3600000000000L, DateTimeUtils.minus(dateTime1, period).getNanos());



        // overflow minus
        DateTimeUtils.minus(new DateTime(Long.MAX_VALUE - 10), -10); // edge at max
        try {
            DateTimeUtils.minus(new DateTime(Long.MAX_VALUE), -1);
            fail("This should have overflowed");
        } catch (DateTimeUtils.DateTimeOverflowException e) {
            // ok
        }

        DateTimeUtils.minus(new DateTime(Long.MIN_VALUE + 10), 10); // edge at min
        try {
            DateTimeUtils.minus(new DateTime(Long.MIN_VALUE), 1);
            fail("This should have overflowed");
        } catch (DateTimeUtils.DateTimeOverflowException e) {
            // ok
        }
    }

    public void testDiff() throws Exception {
        org.joda.time.DateTime jodaDateTime1 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");
        org.joda.time.DateTime jodaDateTime2 = new org.joda.time.DateTime("2010-01-01T13:13:14.999");

        DateTime dateTime1 = new DateTime(jodaDateTime1.getMillis() * 1000000 + 123456);
        DateTime dateTime2 = new DateTime(jodaDateTime2.getMillis() * 1000000 + 123456);

        assertEquals(3600000000000L, DateTimeUtils.diff(dateTime1, dateTime2));
        assertEquals(-3600000000000L, DateTimeUtils.diff(dateTime2, dateTime1));

        assertEquals(3600000000000L, DateTimeUtils.diffNanos(dateTime1, dateTime2));
        assertEquals(-3600000000000L, DateTimeUtils.diffNanos(dateTime2, dateTime1));
    }

    public void testYearDiff() throws Exception {
        org.joda.time.DateTime jt1 = new org.joda.time.DateTime("2010-01-01T12:13:14.999");
        org.joda.time.DateTime jt2 = new org.joda.time.DateTime("2011-01-01T13:13:14.999");
        org.joda.time.DateTime jt3 = new org.joda.time.DateTime("2010-06-30T13:13:14.999");

        DateTime t1 = new DateTime(jt1.getMillis() * 1000000 + 123456);
        DateTime t2 = new DateTime(jt2.getMillis() * 1000000 + 123456);
        DateTime t3 = new DateTime(jt3.getMillis() * 1000000 + 123456);


        assertEquals(1.0, DateTimeUtils.yearDiff(t1, t2), 0.01);
        assertEquals(0.5, DateTimeUtils.yearDiff(t1, t3), 0.01);
        assertEquals(io.deephaven.util.QueryConstants.NULL_DOUBLE, DateTimeUtils.yearDiff(null, t1));
        assertEquals(io.deephaven.util.QueryConstants.NULL_DOUBLE, DateTimeUtils.yearDiff(t1, null));

        assertEquals(1.0, DateTimeUtils.diffYear(t1, t2), 0.01);
        assertEquals(0.5, DateTimeUtils.diffYear(t1, t3), 0.01);
        assertEquals(io.deephaven.util.QueryConstants.NULL_DOUBLE, DateTimeUtils.diffYear(null, t1));
        assertEquals(io.deephaven.util.QueryConstants.NULL_DOUBLE, DateTimeUtils.diffYear(t1, null));
    }

    public void testMillisToNanos() throws Exception {
        assertEquals(1000000, DateTimeUtils.millisToNanos(1));

        // The next two tests will fail if DateTimeUtils.ENABLE_MICROTIME_HACK is true
        try {
            DateTimeUtils.millisToNanos(Long.MAX_VALUE / 1_000_000 + 1);
            fail("Should have thrown a DateTimeUtils.DateTimeOverflowException");
        } catch (DateTimeUtils.DateTimeOverflowException ignored) {
            /* Exception is expected. */
        }

        try {
            DateTimeUtils.millisToNanos(-Long.MAX_VALUE / 1_000_000 - 1);
            fail("Should have thrown a DateTimeUtils.DateTimeOverflowException");
        } catch (DateTimeUtils.DateTimeOverflowException ignored) {
            /* Exception is expected. */
        }
    }

    /*
     * public void testMillisToNanosWithHack() throws Exception { // For this to pass, ENABLE_MICROTIME_HACK in
     * DateTimeUtils must be true (i.e. you have // to run the tests with -DDateTimeUtils.enableMicrotimeHack=true)
     * assertEquals(1_000_000, DateTimeUtils.millisToNanos(1)); assertEquals(1_000_000_000,
     * DateTimeUtils.millisToNanos(1_000)); assertEquals(1531315655_000_000_000L,
     * DateTimeUtils.millisToNanos(1531315655_000L)); assertEquals(1531315655_000_000_000L,
     * DateTimeUtils.millisToNanos(1531315655_000_000L)); }
     */

    public void testNanosToMillis() throws Exception {
        assertEquals(1, DateTimeUtils.nanosToMillis(1000000));
    }

    public void testMicroToNanos() throws Exception {
        assertEquals(1000, DateTimeUtils.microsToNanos(1));

        try {
            DateTimeUtils.microsToNanos(Long.MAX_VALUE / 1_000 + 1);
            fail("Should have thrown a DateTimeUtils.DateTimeOverflowException");
        } catch (DateTimeUtils.DateTimeOverflowException ignored) {
            /* Exception is expected. */
        }

        try {
            DateTimeUtils.microsToNanos(-Long.MAX_VALUE / 1_000 - 1);
            fail("Should have thrown a DateTimeUtils.DateTimeOverflowException");
        } catch (DateTimeUtils.DateTimeOverflowException ignored) {
            /* Exception is expected. */
        }
    }

    public void testNanosToMicros() throws Exception {
        assertEquals(1, DateTimeUtils.nanosToMicros(1000));
    }

    public void testConvertDateQuiet() throws Exception {
        // ISO formats
        assertEquals(LocalDate.of(2018, 1, 1), DateTimeUtils.convertDateQuiet("2018-01-01"));
        assertEquals(LocalDate.of(2018, 12, 31), DateTimeUtils.convertDateQuiet("2018-12-31"));
        assertEquals(LocalDate.of(2018, 1, 1), DateTimeUtils.convertDateQuiet("20180101"));
        assertEquals(LocalDate.of(2018, 12, 31), DateTimeUtils.convertDateQuiet("20181231"));

        // extremities of the format (LocalDate can store a much larger range than this but we aren't that interested)
        assertEquals(LocalDate.of(0, 1, 1), DateTimeUtils.convertDateQuiet("0000-01-01"));
        assertEquals(LocalDate.of(9999, 12, 31), DateTimeUtils.convertDateQuiet("9999-12-31"));

        // other variants
        assertEquals(LocalDate.of(2018, 1, 1),
                DateTimeUtils.convertDateQuiet("01/01/2018", DateTimeUtils.DateStyle.MDY));
        assertEquals(LocalDate.of(2018, 12, 31),
                DateTimeUtils.convertDateQuiet("12/31/2018", DateTimeUtils.DateStyle.MDY));
        assertEquals(LocalDate.of(2018, 12, 31),
                DateTimeUtils.convertDateQuiet("12/31/18", DateTimeUtils.DateStyle.MDY));
        assertEquals(LocalDate.of(2024, 6, 25), DateTimeUtils.convertDateQuiet("6/25/24", DateTimeUtils.DateStyle.MDY));
        assertEquals(LocalDate.of(2024, 6, 2), DateTimeUtils.convertDateQuiet("6/2/24", DateTimeUtils.DateStyle.MDY));
        assertEquals(LocalDate.of(2024, 6, 2), DateTimeUtils.convertDateQuiet("6/2/2024", DateTimeUtils.DateStyle.MDY));

        assertEquals(LocalDate.of(2018, 1, 1),
                DateTimeUtils.convertDateQuiet("01/01/2018", DateTimeUtils.DateStyle.DMY));
        assertEquals(LocalDate.of(2018, 12, 31),
                DateTimeUtils.convertDateQuiet("31/12/2018", DateTimeUtils.DateStyle.DMY));
        assertEquals(LocalDate.of(2018, 12, 31),
                DateTimeUtils.convertDateQuiet("31/12/18", DateTimeUtils.DateStyle.DMY));
        assertEquals(LocalDate.of(2024, 6, 25), DateTimeUtils.convertDateQuiet("25/6/24", DateTimeUtils.DateStyle.DMY));
        assertEquals(LocalDate.of(2024, 6, 2), DateTimeUtils.convertDateQuiet("2/6/24", DateTimeUtils.DateStyle.DMY));
        assertEquals(LocalDate.of(2024, 6, 2), DateTimeUtils.convertDateQuiet("2/6/2024", DateTimeUtils.DateStyle.DMY));


        assertEquals(LocalDate.of(2018, 1, 1),
                DateTimeUtils.convertDateQuiet("2018/01/01", DateTimeUtils.DateStyle.YMD));
        assertEquals(LocalDate.of(2018, 12, 31),
                DateTimeUtils.convertDateQuiet("2018/12/31", DateTimeUtils.DateStyle.YMD));
        assertEquals(LocalDate.of(2018, 12, 31),
                DateTimeUtils.convertDateQuiet("18/12/31", DateTimeUtils.DateStyle.YMD));
        assertEquals(LocalDate.of(2024, 6, 25), DateTimeUtils.convertDateQuiet("24/6/25", DateTimeUtils.DateStyle.YMD));
        assertEquals(LocalDate.of(2024, 6, 2), DateTimeUtils.convertDateQuiet("24/6/2", DateTimeUtils.DateStyle.YMD));
        assertEquals(LocalDate.of(2024, 6, 2), DateTimeUtils.convertDateQuiet("2024/6/2", DateTimeUtils.DateStyle.YMD));
    }

    public void testConvertLocalTimeQuiet() throws Exception {

        assertEquals(java.time.LocalTime.of(12, 59, 59), DateTimeUtils.convertLocalTimeQuiet("L12:59:59"));
        assertEquals(java.time.LocalTime.of(0, 0, 0), DateTimeUtils.convertLocalTimeQuiet("L00:00:00"));
        assertEquals(java.time.LocalTime.of(23, 59, 59), DateTimeUtils.convertLocalTimeQuiet("L23:59:59"));

        assertEquals(java.time.LocalTime.of(12, 59, 59), DateTimeUtils.convertLocalTimeQuiet("L125959"));
        assertEquals(java.time.LocalTime.of(0, 0, 0), DateTimeUtils.convertLocalTimeQuiet("L000000"));
        assertEquals(java.time.LocalTime.of(23, 59, 59), DateTimeUtils.convertLocalTimeQuiet("L235959"));

        assertEquals(java.time.LocalTime.of(12, 0, 0), DateTimeUtils.convertLocalTimeQuiet("L12"));
        assertEquals(java.time.LocalTime.of(12, 59, 0), DateTimeUtils.convertLocalTimeQuiet("L12:59"));
        assertEquals(java.time.LocalTime.of(12, 59, 59, 123_000_000),
                DateTimeUtils.convertLocalTimeQuiet("L12:59:59.123"));
        assertEquals(java.time.LocalTime.of(12, 59, 59, 123_456_000),
                DateTimeUtils.convertLocalTimeQuiet("L12:59:59.123456"));
        assertEquals(java.time.LocalTime.of(12, 59, 59, 123_456_789),
                DateTimeUtils.convertLocalTimeQuiet("L12:59:59.123456789"));

        assertEquals(java.time.LocalTime.of(12, 0, 0), DateTimeUtils.convertLocalTimeQuiet("L12"));
        assertEquals(java.time.LocalTime.of(12, 59, 0), DateTimeUtils.convertLocalTimeQuiet("L1259"));
        assertEquals(java.time.LocalTime.of(12, 59, 59, 123_000_000),
                DateTimeUtils.convertLocalTimeQuiet("L125959.123"));
        assertEquals(java.time.LocalTime.of(12, 59, 59, 123_456_000),
                DateTimeUtils.convertLocalTimeQuiet("L125959.123456"));
        assertEquals(java.time.LocalTime.of(12, 59, 59, 123_456_789),
                DateTimeUtils.convertLocalTimeQuiet("L125959.123456789"));
    }

    public void testConvertDate() throws Exception {
        DateTimeUtils.convertDate("2010-01-01"); // shouldn't have an exception

        try {
            DateTimeUtils.convertDate("2010-01-01 NY");
            fail("Should have thrown an exception");
        } catch (Exception e) {
        }

        assertEquals("DateTimeUtils.convertDate(\"9999-12-31\")",
                LocalDate.of(9999, 12, 31),
                DateTimeUtils.convertDate("9999-12-31"));
    }

    public void testConvertDateTimeQuiet() throws Exception {
        assertEquals(
                new DateTime(
                        new org.joda.time.DateTime("2010-01-01", DateTimeZone.forID("America/New_York")).getMillis()
                                * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01 NY"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00", DateTimeZone.forID("America/New_York")).getMillis()
                        * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00 NY"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00.1", DateTimeZone.forID("America/New_York")).getMillis()
                        * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.1 NY"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00.123", DateTimeZone.forID("America/New_York"))
                        .getMillis() * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123 NY"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00.123", DateTimeZone.forID("America/New_York"))
                        .getMillis() * 1000000
                        + 400000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.1234 NY"));
        assertEquals(
                new DateTime(
                        new org.joda.time.DateTime("2010-01-01T12:00:00.123", DateTimeZone.forID("America/New_York"))
                                .getMillis()
                                * 1000000 + 456789),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123456789 NY"));

        assertEquals(
                new DateTime(new org.joda.time.DateTime("2010-01-01", DateTimeZone.forID("America/Chicago")).getMillis()
                        * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01 MN"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00", DateTimeZone.forID("America/Chicago")).getMillis()
                        * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00 MN"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00.1", DateTimeZone.forID("America/Chicago")).getMillis()
                        * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.1 MN"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00.123", DateTimeZone.forID("America/Chicago")).getMillis()
                        * 1000000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123 MN"));
        assertEquals(new DateTime(
                new org.joda.time.DateTime("2010-01-01T12:00:00.123", DateTimeZone.forID("America/Chicago")).getMillis()
                        * 1000000
                        + 400000),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.1234 MN"));
        assertEquals(
                new DateTime(
                        new org.joda.time.DateTime("2010-01-01T12:00:00.123", DateTimeZone.forID("America/Chicago"))
                                .getMillis()
                                * 1000000 + 456789),
                DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123456789 MN"));

        assertEquals(new DateTime(1503343549064106107L),
                DateTimeUtils.convertDateTimeQuiet("2017-08-21T15:25:49.064106107 NY"));

        // assertEquals(new DateTime(new DateTime("2010-01-01T12:00:00.123", DateTimeZone.UTC).getMillis()*1000000),
        // DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123+0000"));
        // assertEquals(new DateTime(new DateTime("2010-01-01T12:00:00.123",
        // DateTimeZone.forID("America/New_York")).getMillis()*1000000),
        // DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123-0400"));
        // assertEquals(new DateTime(new DateTime("2010-01-01T12:00:00.123",
        // DateTimeZone.forID("Asia/Seoul")).getMillis()*1000000),
        // DateTimeUtils.convertDateTimeQuiet("2010-01-01T12:00:00.123+0900"));
    }

    public void testConvertDateTime() throws Exception {
        DateTimeUtils.convertDateTime("2010-01-01 NY"); // shouldn't have an exception

        try {
            DateTimeUtils.convertDateTime("2010-01-01");
            fail("Should have thrown an exception");
        } catch (Exception e) {
        }

        assertEquals("DateTimeUtils.convertDateTime(\"2262-04-11T19:47:16.854775807 NY\").getNanos()",
                Long.MAX_VALUE,
                DateTimeUtils.convertDateTime("2262-04-11T19:47:16.854775807 NY").getNanos());
    }

    public void testConvertTimeQuiet() throws Exception {
        assertEquals(new LocalTime("12:00").getMillisOfDay() * 1000000L, DateTimeUtils.convertTimeQuiet("12:00"));
        assertEquals(new LocalTime("12:00:00").getMillisOfDay() * 1000000L, DateTimeUtils.convertTimeQuiet("12:00:00"));
        assertEquals(new LocalTime("12:00:00.123").getMillisOfDay() * 1000000L,
                DateTimeUtils.convertTimeQuiet("12:00:00.123"));
        assertEquals(new LocalTime("12:00:00.123").getMillisOfDay() * 1000000L + 400000,
                DateTimeUtils.convertTimeQuiet("12:00:00.1234"));
        assertEquals(new LocalTime("12:00:00.123").getMillisOfDay() * 1000000L + 456789,
                DateTimeUtils.convertTimeQuiet("12:00:00.123456789"));

        assertEquals(new LocalTime("2:00").getMillisOfDay() * 1000000L, DateTimeUtils.convertTimeQuiet("2:00"));
        assertEquals(new LocalTime("2:00:00").getMillisOfDay() * 1000000L, DateTimeUtils.convertTimeQuiet("2:00:00"));
        assertEquals(new LocalTime("2:00:00.123").getMillisOfDay() * 1000000L,
                DateTimeUtils.convertTimeQuiet("2:00:00.123"));
        assertEquals(new LocalTime("2:00:00.123").getMillisOfDay() * 1000000L + 400000,
                DateTimeUtils.convertTimeQuiet("2:00:00.1234"));
        assertEquals(new LocalTime("2:00:00.123").getMillisOfDay() * 1000000L + 456789,
                DateTimeUtils.convertTimeQuiet("2:00:00.123456789"));

        assertEquals(new LocalTime("2:00").getMillisOfDay() * 1000000L + 3L * 1000000 * DateUtil.MILLIS_PER_DAY,
                DateTimeUtils.convertTimeQuiet("3T2:00"));
        assertEquals(new LocalTime("2:00:00").getMillisOfDay() * 1000000L + 3L * 1000000 * DateUtil.MILLIS_PER_DAY,
                DateTimeUtils.convertTimeQuiet("3T2:00:00"));
        assertEquals(new LocalTime("2:00:00.123").getMillisOfDay() * 1000000L + 3L * 1000000 * DateUtil.MILLIS_PER_DAY,
                DateTimeUtils.convertTimeQuiet("3T2:00:00.123"));
        assertEquals(new LocalTime("2:00:00.123").getMillisOfDay() * 1000000L + 400000
                + 3L * 1000000 * DateUtil.MILLIS_PER_DAY, DateTimeUtils.convertTimeQuiet("3T2:00:00.1234"));
        assertEquals(new LocalTime("2:00:00.123").getMillisOfDay() * 1000000L + 456789
                + 3L * 1000000 * DateUtil.MILLIS_PER_DAY, DateTimeUtils.convertTimeQuiet("3T2:00:00.123456789"));

        assertEquals(55549064106107L, DateTimeUtils.convertTimeQuiet("15:25:49.064106107"));
    }

    public void testConvertTime() throws Exception {
        DateTimeUtils.convertTime("12:00"); // shouldn't have an exception

        try {
            DateTimeUtils.convertTime("12");
            fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    public void testConvertPeriodQuiet() throws Exception {
        assertEquals(new org.joda.time.Period("PT1s"), DateTimeUtils.convertPeriodQuiet("T1S").getJodaPeriod());
        assertEquals(new org.joda.time.Period("P1wT1m"), DateTimeUtils.convertPeriodQuiet("1WT1M").getJodaPeriod());
        assertEquals(new org.joda.time.Period("P1w"), DateTimeUtils.convertPeriodQuiet("1W").getJodaPeriod());

        assertEquals(null, DateTimeUtils.convertPeriodQuiet("-"));
    }

    public void testConvertPeriod() throws Exception {
        DateTimeUtils.convertPeriod("T1S"); // shouldn't have an exception

        try {
            DateTimeUtils.convertPeriod("1S");
            fail("Should have thrown an exception");
        } catch (Exception e) {
        }
    }

    public void testTimeFormat() throws Exception {
        assertEquals("12:00:00", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("12:00")));
        assertEquals("12:00:00", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("12:00:00")));
        assertEquals("12:00:00.123000000", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("12:00:00.123")));
        assertEquals("12:00:00.123400000", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("12:00:00.1234")));
        assertEquals("12:00:00.123456789", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("12:00:00.123456789")));

        assertEquals("2:00:00", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("2:00")));
        assertEquals("2:00:00", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("2:00:00")));
        assertEquals("2:00:00.123000000", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("2:00:00.123")));
        assertEquals("2:00:00.123400000", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("2:00:00.1234")));
        assertEquals("2:00:00.123456789", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("2:00:00.123456789")));

        assertEquals("3T2:00:00", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("3T2:00")));
        assertEquals("3T2:00:00", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("3T2:00:00")));
        assertEquals("3T2:00:00.123000000", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("3T2:00:00.123")));
        assertEquals("3T2:00:00.123400000", DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("3T2:00:00.1234")));
        assertEquals("3T2:00:00.123456789",
                DateTimeUtils.format(DateTimeUtils.convertTimeQuiet("3T2:00:00.123456789")));
    }

    public void testFormatDate() throws Exception {
        assertEquals("2010-01-01",
                DateTimeUtils.formatDate(DateTimeUtils.convertDateTimeQuiet("2010-01-01 NY"), TimeZone.TZ_NY));
    }

    public void testLowerBin() {
        final long second = 1000000000L;
        final long minute = 60 * second;
        final long hour = 60 * minute;
        DateTime time = DateTimeUtils.convertDateTime("2010-06-15T06:14:01.2345 NY");

        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:14:01 NY"), DateTimeUtils.lowerBin(time, second));
        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:10:00 NY"), DateTimeUtils.lowerBin(time, 5 * minute));
        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:00:00 NY"), DateTimeUtils.lowerBin(time, hour));
        assertEquals(null, DateTimeUtils.lowerBin(null, 5 * minute));
        assertEquals(null, DateTimeUtils.lowerBin(time, io.deephaven.util.QueryConstants.NULL_LONG));

        assertEquals(DateTimeUtils.lowerBin(time, second),
                DateTimeUtils.lowerBin(DateTimeUtils.lowerBin(time, second), second));
    }

    public void testLowerBinWithOffset() {
        final long second = 1000000000L;
        final long minute = 60 * second;
        final long hour = 60 * minute;
        DateTime time = DateTimeUtils.convertDateTime("2010-06-15T06:14:01.2345 NY");

        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:11:00 NY"),
                DateTimeUtils.lowerBin(time, 5 * minute, minute));
        assertEquals(null, DateTimeUtils.lowerBin(null, 5 * minute, minute));
        assertEquals(null, DateTimeUtils.lowerBin(time, QueryConstants.NULL_LONG, minute));
        assertEquals(null, DateTimeUtils.lowerBin(time, 5 * minute, QueryConstants.NULL_LONG));

        assertEquals(DateTimeUtils.lowerBin(time, second, second),
                DateTimeUtils.lowerBin(DateTimeUtils.lowerBin(time, second, second), second, second));
    }

    public void testUpperBin() {
        final long second = 1000000000L;
        final long minute = 60 * second;
        final long hour = 60 * minute;
        DateTime time = DateTimeUtils.convertDateTime("2010-06-15T06:14:01.2345 NY");

        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:14:02 NY"), DateTimeUtils.upperBin(time, second));
        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:15:00 NY"), DateTimeUtils.upperBin(time, 5 * minute));
        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T07:00:00 NY"), DateTimeUtils.upperBin(time, hour));
        assertEquals(null, DateTimeUtils.upperBin(null, 5 * minute));
        assertEquals(null, DateTimeUtils.upperBin(time, io.deephaven.util.QueryConstants.NULL_LONG));

        assertEquals(DateTimeUtils.upperBin(time, second),
                DateTimeUtils.upperBin(DateTimeUtils.upperBin(time, second), second));
    }

    public void testUpperBinWithOffset() {
        final long second = 1000000000L;
        final long minute = 60 * second;
        final long hour = 60 * minute;
        DateTime time = DateTimeUtils.convertDateTime("2010-06-15T06:14:01.2345 NY");

        assertEquals(DateTimeUtils.convertDateTime("2010-06-15T06:16:00 NY"),
                DateTimeUtils.upperBin(time, 5 * minute, minute));
        assertEquals(null, DateTimeUtils.upperBin(null, 5 * minute, minute));
        assertEquals(null, DateTimeUtils.upperBin(time, io.deephaven.util.QueryConstants.NULL_LONG, minute));
        assertEquals(null, DateTimeUtils.upperBin(time, 5 * minute, QueryConstants.NULL_LONG));

        assertEquals(DateTimeUtils.upperBin(time, second, second),
                DateTimeUtils.upperBin(DateTimeUtils.upperBin(time, second, second), second, second));
    }

    public void testConvertJimDateTimeQuiet() {
        String s = "2010-09-02T08:17:17.502-0400";
        DateTime known = DateTimeUtils.convertDateTimeQuiet(s);
        DateTime trial = DateTimeUtils.convertJimDateTimeQuiet(s);
        assertEquals(known, trial);
    }

    public void testGetExcelDate() {
        DateTime time = DateTimeUtils.convertDateTime("2010-06-15T16:00:00 NY");
        assertTrue(CompareUtils.doubleEquals(40344.666666666664, DateTimeUtils.getExcelDateTime(time)));
        assertTrue(CompareUtils.doubleEquals(40344.625, DateTimeUtils.getExcelDateTime(time, TimeZones.TZ_CHICAGO)));
        assertTrue(CompareUtils.doubleEquals(40344.625, DateTimeUtils.getExcelDateTime(time, TimeZone.TZ_MN)));
    }

    /**
     * Test autoEpcohTime with the given epoch time.
     * 
     * @param epoch Epoch time (in seconds)
     * @return The year (in the New York timezone) in which the given time falls.
     */
    public int doTestAutoEpochToTime(long epoch) {
        assertEquals(DateTimeUtils.autoEpochToTime(epoch).getMillis(), epoch * 1000);
        assertEquals(DateTimeUtils.autoEpochToTime(epoch).getMicros(), epoch * 1000 * 1000);
        assertEquals(DateTimeUtils.autoEpochToTime(epoch).getNanos(), epoch * 1000 * 1000 * 1000);

        final long milliValue = epoch * 1000 + (int) (Math.signum(epoch) * 123);
        assertEquals(DateTimeUtils.autoEpochToTime(milliValue).getMillis(), milliValue);
        assertEquals(DateTimeUtils.autoEpochToTime(milliValue).getMicros(), milliValue * 1000);
        assertEquals(DateTimeUtils.autoEpochToTime(milliValue).getNanos(), milliValue * 1000 * 1000);

        final long microValue = milliValue * 1000 + (int) (Math.signum(milliValue) * 456);
        assertEquals(DateTimeUtils.autoEpochToTime(microValue).getMillis(), milliValue);
        assertEquals(DateTimeUtils.autoEpochToTime(microValue).getMicros(), microValue);
        assertEquals(DateTimeUtils.autoEpochToTime(microValue).getNanos(), microValue * 1000);

        final long nanoValue = microValue * 1000 + (int) (Math.signum(microValue) * 789);
        assertEquals(DateTimeUtils.autoEpochToTime(nanoValue).getMillis(), milliValue);
        assertEquals(DateTimeUtils.autoEpochToTime(nanoValue).getMicros(), microValue);
        assertEquals(DateTimeUtils.autoEpochToTime(nanoValue).getNanos(), nanoValue);

        return DateTimeUtils.yearNy(DateTimeUtils.autoEpochToTime(nanoValue));
    }

    public void testAutoEpochToTime() {
        long inTheYear2035 = 2057338800;
        assertEquals("doTestAutoEpochToTime(inTheYear2035)", 2035, doTestAutoEpochToTime(inTheYear2035));
        long inTheYear1993 = 731966400;
        assertEquals("doTestAutoEpochToTime(inTheYear1993)", 1993, doTestAutoEpochToTime(inTheYear1993));
        long inTheYear2013 = 1363114800;
        assertEquals("doTestAutoEpochToTime(inTheYear2013)", 2013, doTestAutoEpochToTime(inTheYear2013));

        long inTheYear1904 = -2057338800;
        assertEquals("doTestAutoEpochToTime(inTheYear1904)", 1904, doTestAutoEpochToTime(inTheYear1904));
        long inTheYear1946 = -731966400;
        assertEquals("doTestAutoEpochToTime(inTheYear1946)", 1946, doTestAutoEpochToTime(inTheYear1946));
        long inTheYear1926 = -1363114800;
        assertEquals("doTestAutoEpochToTime(inTheYear1926)", 1926, doTestAutoEpochToTime(inTheYear1926));
    }

    public void testConvertExpression() throws Exception {
        assertEquals("_date0", DateTimeUtils.convertExpression("'2010-01-01 NY'").getConvertedFormula());
        assertEquals("_time0", DateTimeUtils.convertExpression("'12:00'").getConvertedFormula());
        assertEquals("_period0", DateTimeUtils.convertExpression("'T1S'").getConvertedFormula());
        assertEquals("'g'", DateTimeUtils.convertExpression("'g'").getConvertedFormula());
    }

    public void testMicrosOfMilli() {
        assertEquals(0, DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40 NY")));
        assertEquals(0, DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40:00 NY")));
        assertEquals(0, DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40:00.123 NY")));
        assertEquals(400, DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40:00.1234 NY")));
        assertEquals(456,
                DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40:00.123456 NY")));
        assertEquals(457,
                DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40:00.1234567 NY"))); // this
        // one
        // should
        // round
        // up
        assertEquals(457,
                DateTimeUtils.microsOfMilliNy(DateTimeUtils.convertDateTime("2015-07-31T20:40:00.123456789 NY"))); // this
        // one
        // should
        // round
        // up

    }

    public void testZonedDateTime() {
        final DateTime dateTime1 = DateTimeUtils.convertDateTime("2015-07-31T20:40 NY");
        final ZonedDateTime zonedDateTime1 =
                ZonedDateTime.of(2015, 7, 31, 20, 40, 0, 0, TimeZone.TZ_NY.getTimeZone().toTimeZone().toZoneId());
        assertEquals(zonedDateTime1, DateTimeUtils.getZonedDateTime(dateTime1, TimeZone.TZ_NY));
        assertEquals(dateTime1, DateTimeUtils.toDateTime(zonedDateTime1));

        final DateTime dateTime2 = DateTimeUtils.convertDateTime("2020-07-31T20:40 NY");
        assertEquals(dateTime2, DateTimeUtils.toDateTime(DateTimeUtils.getZonedDateTime(dateTime2, TimeZone.TZ_NY)));

        final DateTime dateTime3 = DateTimeUtils.convertDateTime("2050-07-31T20:40 NY");
        assertEquals(dateTime3, DateTimeUtils.toDateTime(DateTimeUtils.getZonedDateTime(dateTime3, TimeZone.TZ_NY)));
    }
}
