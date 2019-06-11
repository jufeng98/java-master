package org.javamaster.b2c.core.java8;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;

/**
 * @author yudong
 * @date 2019/6/11
 */
public class DateTest {

    @Test
    public void test() {
        LocalDate date = LocalDate.of(2014, 3, 18);
        LocalTime time = LocalTime.of(10, 3, 18);
        int year = date.getYear();
        Month month = date.getMonth();
        int day = date.getDayOfMonth();
        DayOfWeek dow = date.getDayOfWeek();
        int len = date.lengthOfMonth();
        boolean leap = date.isLeapYear();
        // 还可以使用工厂方法从系统时钟中获取当前的日期：
        LocalDate today = LocalDate.now();
        // LocalDateTime，是LocalDate和LocalTime的合体。它同时表示了日期和时间，但不带有时区信息
        LocalDateTime dt1 = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45, 20);
        LocalDateTime dt2 = LocalDateTime.of(date, time);
        // 也可以使用toLocalDate或者toLocalTime方法，从LocalDateTime中提取LocalDate或者LocalTime组件：
        LocalDate date1 = dt1.toLocalDate();
        LocalTime time1 = dt1.toLocalTime();

    }

    @Test
    public void test1() {
        // Duration以秒为单位衡量时间长短
        LocalTime time1 = LocalTime.of(10, 3, 18);
        LocalTime time2 = LocalTime.now();
        Duration d1 = Duration.between(time1, time2);
        LocalDateTime dateTime1 = LocalDateTime.of(2014, 3, 18, 0, 0, 0);
        LocalDateTime dateTime2 = LocalDateTime.now();
        Duration d2 = Duration.between(dateTime1, dateTime2);
        Temporal instant1 = Instant.ofEpochMilli(System.currentTimeMillis() - 100000);
        Temporal instant2 = Instant.now();
        Duration d3 = Duration.between(instant1, instant2);
    }

    @Test
    public void test2() {
        Period tenDays = Period.between(LocalDate.of(2014, 3, 8), LocalDate.of(2014, 3, 18));
        // Duration和Period类都提供了很多非常方便的工厂类，直接创建对应的实例:
        Duration threeMinutes = Duration.ofMinutes(3);
        threeMinutes = Duration.of(3, ChronoUnit.MINUTES);
        tenDays = Period.ofDays(10);
        Period threeWeeks = Period.ofWeeks(3);
        Period twoYearsSixMonthsOneDay = Period.of(2, 6, 1);
    }

    @Test
    public void test3() {
        LocalDate date1 = LocalDate.of(2014, 3, 18);
        LocalDate date2 = date1.withYear(2011);
        LocalDate date3 = date2.withDayOfMonth(25);
        LocalDate date4 = date3.with(ChronoField.MONTH_OF_YEAR, 9);
        // 以相对方式创建LocalDate对象修改版
        date1 = LocalDate.of(2014, 3, 18);
        date2 = date1.plusWeeks(1);
        date3 = date2.minusYears(3);
        date4 = date3.plus(6, ChronoUnit.MONTHS);
    }

    @Test
    public void test4() {
        LocalDate date1 = LocalDate.of(2014, 3, 18);
        LocalDate date2 = date1.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate date3 = date2.with(TemporalAdjusters.lastDayOfMonth());
    }

    @Test
    public void test5() {
        LocalDate date = LocalDate.of(2014, 3, 18);
        String s1 = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String s2 = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate date1 = LocalDate.parse("20140318", DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate date2 = LocalDate.parse("2014-03-18", DateTimeFormatter.ISO_LOCAL_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        date1 = LocalDate.of(2014, 3, 18);
        String formattedDate = date1.format(formatter);
        date2 = LocalDate.parse(formattedDate, formatter);

        // UTC时间
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2019, 2, 2, 10, 0, 0, 0, ZoneId.of("UTC"));
        System.out.println(zonedDateTime);// 2019-02-02T10:00Z[UTC]
        // 转换成上海时间
        LocalDateTime dateTime = LocalDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.of("Asia/Shanghai"));//GMT+8
        System.out.println(dateTime);// 2019-02-02T18:00
    }
}
