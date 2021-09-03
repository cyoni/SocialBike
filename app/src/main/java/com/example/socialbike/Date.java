package com.example.socialbike;

public class Date {

    public static long getTimeInMiliSecs(){
        java.util.Date date = new java.util.Date();
        return date.getTime();
    }

    public static String convertMiliToTime(long mili){
        return "@@";
    }
}
