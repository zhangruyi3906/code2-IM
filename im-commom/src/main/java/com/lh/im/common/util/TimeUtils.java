//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.lh.im.common.util;

import cn.hutool.core.lang.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeUtils {
    public static final ZoneId ZONE_ID_CN = ZoneId.of("Asia/Shanghai");
    public static final LocalTime START_OF_DAY = LocalTime.of(0, 0, 0, 0);
    public static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59, 0);
    public static final String YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYY = "yyyy";
    public static final String MM = "MM";
    public static final String DATE = "yyyy-MM-dd";
    public static final String MONTH = "yyyy-MM";
    public static final String DATE_HOUR = "yyyy-MM-dd HH";
    public static final String DATE_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME = "HH:mm:ss";
    public static final DateTimeFormatter YYYYMMDD_FORMATTER;
    public static final DateTimeFormatter YYYYMM_FORMATTER;
    public static final DateTimeFormatter YYYY_FORMATTER;
    public static final DateTimeFormatter MM_FORMATTER;
    public static final DateTimeFormatter DATE_FORMATTER;
    public static final DateTimeFormatter MONTH_FORMATTER;
    public static final DateTimeFormatter DATE_HOUR_FORMATTER;
    public static final DateTimeFormatter DATE_MINUTE_FORMATTER;
    public static final DateTimeFormatter DATE_TIME_FORMATTER;
    public static final DateTimeFormatter TIME_FORMATTER;

    public TimeUtils() {
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZONE_ID_CN);
    }

    public static long currentSecond() {
        return Instant.now().getEpochSecond();
    }

    public static long currentMilli() {
        return Instant.now().toEpochMilli();
    }

    public static String format(Date date, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format).withZone(ZONE_ID_CN);
        return dateTimeFormatter.format(date.toInstant());
    }

    public static String format(Instant instant, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format).withZone(ZONE_ID_CN);
        return dateTimeFormatter.format(instant);
    }

    public static String formatAsYYYYMMDD(Date date) {
        return YYYYMMDD_FORMATTER.format(date.toInstant());
    }

    public static String formatAsYYYYMMDD(Instant instant) {
        return YYYYMMDD_FORMATTER.format(instant);
    }

    public static String formatAsYYYYMM(Date date) {
        return YYYYMM_FORMATTER.format(date.toInstant());
    }

    public static String formatAsYYYYMM(Instant instant) {
        return YYYYMM_FORMATTER.format(instant);
    }

    public static String formatAsYYYY(Date date) {
        return YYYY_FORMATTER.format(date.toInstant());
    }

    public static String formatAsYYYY(Instant instant) {
        return YYYY_FORMATTER.format(instant);
    }

    public static String formatAsMM(Date date) {
        return MM_FORMATTER.format(date.toInstant());
    }

    public static String formatAsMM(Instant instant) {
        return MM_FORMATTER.format(instant);
    }

    public static String formatAsMonth(Date date) {
        return MONTH_FORMATTER.format(date.toInstant());
    }

    public static String formatAsMonth(Instant instant) {
        return MONTH_FORMATTER.format(instant);
    }

    public static String formatAsDate(Date date) {
        return DATE_FORMATTER.format(date.toInstant());
    }

    public static String formatAsDate(Instant instant) {
        return DATE_FORMATTER.format(instant);
    }

    public static String formatAsTime(Date date) {
        return TIME_FORMATTER.format(date.toInstant());
    }

    public static String formatAsTime(Instant instant) {
        return TIME_FORMATTER.format(instant);
    }

    public static String formatAsHour(Date date) {
        return DATE_HOUR_FORMATTER.format(date.toInstant());
    }

    public static String formatAsHour(Instant instant) {
        return DATE_HOUR_FORMATTER.format(instant);
    }

    public static String formatAsDateMinute(Date date) {
        return DATE_MINUTE_FORMATTER.format(date.toInstant());
    }

    public static String formatAsDateMinute(Instant instant) {
        return DATE_MINUTE_FORMATTER.format(instant);
    }

    public static String formatAsDateTime(Date date) {
        return DATE_TIME_FORMATTER.format(date.toInstant());
    }

    public static String formatAsDateTime(Instant instant) {
        return DATE_TIME_FORMATTER.format(instant);
    }

    public static ZonedDateTime ofDate(Date date) {
        return date.toInstant().atZone(ZONE_ID_CN);
    }

    public static ZonedDateTime ofMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZONE_ID_CN);
    }

    public static ZonedDateTime ofSecond(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond).atZone(ZONE_ID_CN);
    }

    public static Date toDate(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZONE_ID_CN).toInstant());
    }

    public static Date parseAsDate(String str) {
        Assert.notBlank(str, "Date must not be null");
        return Date.from(ZonedDateTime.parse(str, DATE_TIME_FORMATTER).toInstant());
    }

    public static Date parseAsDate(String str, String pattern) {
        Assert.isFalse(StringUtils.isAnyBlank(new CharSequence[]{str, pattern}), "Date and Patterns must not be null");

        try {
            return DateUtils.parseDate(str, new String[]{pattern});
        } catch (ParseException var3) {
            throw new RuntimeException(var3.getMessage(), var3);
        }
    }

    public static ZonedDateTime startOfMonth(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfMonth()).with(START_OF_DAY);
    }

    public static ZonedDateTime startOfYear(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.firstDayOfYear()).with(START_OF_DAY);
    }

    public static ZonedDateTime endOfYear(ZonedDateTime dateTime) {
        return dateTime.with(TemporalAdjusters.lastDayOfYear()).with(END_OF_DAY);
    }

    public static Date getMonthStartDate(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.with(TemporalAdjusters.firstDayOfMonth()).with(START_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getMonthEndDate(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.with(TemporalAdjusters.lastDayOfMonth()).with(END_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getWeekStartDate(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.with(DayOfWeek.of(1)).with(START_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getWeekEndDate(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.with(DayOfWeek.of(7)).with(END_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getLastMonthStartDate() {
        Instant instant = now().minusMonths(1L).with(TemporalAdjusters.firstDayOfMonth()).with(START_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getLastMonthEndDate() {
        Instant instant = now().minusMonths(1L).with(TemporalAdjusters.lastDayOfMonth()).with(END_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getDayStart(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.with(START_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static Date getDayEnd(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.with(END_OF_DAY).toInstant();
        return Date.from(instant);
    }

    public static long daysBetween(Date start, Date end) {
        LocalDate startDate = ZonedDateTime.ofInstant(start.toInstant(), ZONE_ID_CN).toLocalDate();
        LocalDate endDate = ZonedDateTime.ofInstant(end.toInstant(), ZONE_ID_CN).toLocalDate();
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static String plusMonths(String month, long monthsToAdd) {
        YearMonth yearMonth = YearMonth.parse(month, YYYYMM_FORMATTER);
        return yearMonth.plusMonths(monthsToAdd).format(YYYYMM_FORMATTER);
    }

    public static String minusMonths(String month, long monthsToMinus) {
        YearMonth yearMonth = YearMonth.parse(month, YYYYMM_FORMATTER);
        return yearMonth.minusMonths(monthsToMinus).format(YYYYMM_FORMATTER);
    }

    public static Date plusDays(Date date, long daysToAdd) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.plusDays(daysToAdd).toInstant();
        return Date.from(instant);
    }

    public static Date minusDays(Date date, long daysToMinus) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.minusDays(daysToMinus).toInstant();
        return Date.from(instant);
    }

    public static Date plusHours(Date date, long hoursToAdd) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.plusHours(hoursToAdd).toInstant();
        return Date.from(instant);
    }

    public static Date minusHours(Date date, long hoursToMinus) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.minusHours(hoursToMinus).toInstant();
        return Date.from(instant);
    }

    public static Date plusMinutes(Date date, long minutesToAdd) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.plusMinutes(minutesToAdd).toInstant();
        return Date.from(instant);
    }

    public static Date minusMinutes(Date date, long minutesToMinus) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZONE_ID_CN);
        Instant instant = dateTime.minusMinutes(minutesToMinus).toInstant();
        return Date.from(instant);
    }

    public static List<String> getMonthsBetween(String startMonth, String endMonth) {
        YearMonth startYearMonth = YearMonth.parse(startMonth, YYYYMM_FORMATTER);
        YearMonth endYearMonth = YearMonth.parse(endMonth, YYYYMM_FORMATTER);
        List<String> months = new ArrayList();

        for(YearMonth month = startYearMonth; !month.isAfter(endYearMonth); month = month.plusMonths(1L)) {
            months.add(month.format(YYYYMM_FORMATTER));
        }

        return months;
    }

    public static int restDaysOfMonth(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZONE_ID_CN).toLocalDate();
        int dayOfMonth = localDate.getDayOfMonth();
        int lengthOfMonth = localDate.lengthOfMonth();
        return lengthOfMonth - dayOfMonth + 1;
    }

    public static int lengthOfMonth(Date date) {
        return date.toInstant().atZone(ZONE_ID_CN).toLocalDate().lengthOfMonth();
    }

    static {
        YYYYMMDD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZONE_ID_CN);
        YYYYMM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM").withZone(ZONE_ID_CN);
        YYYY_FORMATTER = DateTimeFormatter.ofPattern("yyyy").withZone(ZONE_ID_CN);
        MM_FORMATTER = DateTimeFormatter.ofPattern("MM").withZone(ZONE_ID_CN);
        DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZONE_ID_CN);
        MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZONE_ID_CN);
        DATE_HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH").withZone(ZONE_ID_CN);
        DATE_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZONE_ID_CN);
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZONE_ID_CN);
        TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZONE_ID_CN);
    }
}
