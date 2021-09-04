package com.example.socialbike;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Locale;

public class Date {

    public static long getTimeInMiliSecs(){
        java.util.Date date = new java.util.Date();
        return date.getTime();
    }

    public static String convertMiliToTime(long timeStamp){
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
}
