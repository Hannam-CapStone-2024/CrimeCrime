package com.example.navigatorteam;

import com.example.navigatorteam.Class.TimeRange;
import com.example.navigatorteam.Class.WeekType;
import com.example.navigatorteam.Support.CrimeType;

public class EachState {
    private CrimeType crimeType;
    private TimeRange timeRange;
    private WeekType weekType;

    public EachState(CrimeType crimeType, TimeRange timeRange, WeekType weekType) {
        this.crimeType = crimeType;
        this.timeRange = timeRange;
        this.weekType = weekType;
    }

    public String getState() {
        // 로직을 추가하여 적절한 상태를 반환합니다.
        // 예시로 "보통"을 반환하도록 합니다.
        return "보통";
    }
}
