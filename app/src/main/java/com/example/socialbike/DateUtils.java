package com.example.socialbike;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static long getTimeInMiliSecs() {
        java.util.Date date = new java.util.Date();
        return date.getTime();
    }

    public static String convertDateToDay(String input_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH);
        java.util.Date myDate = null;
        try {
            myDate = sdf.parse(input_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.applyPattern("EEE, d MMM yyyy");
        return sdf.format(myDate);
/*        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        java.util.Date dt1 = null;
        try {
            dt1 = format1.parse(input_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat format2 = new SimpleDateFormat("EEEE");
        return format2.format(dt1);*/
    }

    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new java.util.Date());
    }

    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        return sdf.format(new java.util.Date());
    }

    public static String convertMiliToDateTime(long timestamp, String pattern) {
        Date date = new Date(timestamp);
        SimpleDateFormat timeZoneDate = new SimpleDateFormat(pattern, Locale.getDefault());
        return timeZoneDate.format(date);
    }
//"EEE, dd-MM-yyyy  hh:mm a"
    public static String convertMiliToTime(long timeStamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        String am_pm = calendar.get(Calendar.AM_PM) == Calendar.PM ? "PM" : "AM";

        return String.format(Locale.US, "%d:%d %s", hour, minute, am_pm);
    }

    public static String convertTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm");
        try {
            java.util.Date d = sdf.parse(time);
            DateFormat dateFormat = new SimpleDateFormat("h:mm aa");
            return dateFormat.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long convertTimes(String source, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
        LocalDateTime dateTime = LocalDateTime.parse(source, formatter);
        ZonedDateTime zdt = dateTime.atZone(OffsetDateTime.now().getOffset());
        return zdt.toInstant().toEpochMilli();
    }
}
