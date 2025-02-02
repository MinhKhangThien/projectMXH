package com.example.projectmxh.utils;

import android.text.format.DateUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static String getTimeAgo(Date date) {
        if (date == null) return "";

        long time = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }

    public static String formatRelativeTime(String createdAt) {
        try {
            // Parse thời gian từ chuỗi JSON
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(createdAt);
            if (date != null) {
                long time = date.getTime();
                // Sử dụng DateUtils.getRelativeTimeSpanString
                return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }


}
