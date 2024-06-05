package com.example.navigatorteam.Class;

import android.icu.util.Calendar;

public enum WeekType {
    Mon("월요일"),
    Tue("화요일"),
    Wed("수요일"),
    Thu("목요일"),
    Fri("금요일"),
    Sat("토요일"),
    Sun("일요일");

    private final String koreanName;

    WeekType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static String getKoreanDay(WeekType day) {
        return day.getKoreanName();
    }

    public static void main(String[] args) {
        for (WeekType day : WeekType.values()) {
            System.out.println(day + " : " + getKoreanDay(day));
        }
    }

    public static String getCurrentWeekType() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "월요일";
            case Calendar.TUESDAY:
                return "화요일";
            case Calendar.WEDNESDAY:
                return "수요일";
            case Calendar.THURSDAY:
                return "목요일";
            case Calendar.FRIDAY:
                return "금요일";
            case Calendar.SATURDAY:
                return "토요일";
            case Calendar.SUNDAY:
                return "일요일";
            default:
                throw new IllegalArgumentException("알 수 없는 요일: " + dayOfWeek);
        }
    }

}
