package com.example.navigatorteam.Class;


import static com.example.navigatorteam.Class.CrimeState.HIGH;
import static com.example.navigatorteam.Class.CrimeState.LOW;
import static com.example.navigatorteam.Class.CrimeState.MIDDLE;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.navigatorteam.Manager.CriminalLoader;
import com.example.navigatorteam.Support.Average;
import com.example.navigatorteam.Support.CrimeType;

public class Crime
{
    public static String TotalState(TimeRange time, WeekType weekType) throws IOException {
        // 각 범죄 유형에 대한 평균 범죄 상태를 저장할 맵
        Map<CrimeType, String> crimeStateMap = new HashMap<>();

        // 모든 범죄 유형에 대해 EachState 메서드를 호출하여 평균 범죄 상태를 계산하고 맵에 저장
        for (CrimeType crimeType : CrimeType.values()) {
            String crimeState = EachState(crimeType, time, weekType);
            Log.v("Taasdad :", crimeStateMap.toString());
            crimeStateMap.put(crimeType, crimeState);
        }

        // 모든 범죄 유형에 대한 평균 범죄 상태를 종합하여 최종 범죄 상태 계산
        double totalAvgCount = 0;
        for (String state : crimeStateMap.values()) {
            if (state.equals("좋음")) {
                totalAvgCount += 0.33;
            } else if (state.equals("보통")) {
                totalAvgCount += 0.66;
            } else if (state.equals("나쁨")) {
                totalAvgCount += 1.0;
            }
        }

        // 전체 평균 범죄 상태 계산
        double avgCrimeState = totalAvgCount / 6;
        System.out.println("avgCrimeState: " + avgCrimeState);
        // 평균 범죄 상태에 따른 최종 범죄 상태 반환
        return calculateCrimeState(avgCrimeState);
    }

    public static String EachState(CrimeType crimeType, TimeRange time, WeekType weekType) throws IOException {
        Map<CrimeType, Integer> crimeSumMap;
        Log.v("Taasdad :", "Caox1");
        try {
            crimeSumMap = CriminalLoader.Avg(); // 범죄 발생 데이터의 평균을 가져옴
        } catch (IOException e) {
            Log.v("Errrrrrr :", e.toString());
            e.printStackTrace();
            return null;
        }
        Log.v("Taasdad :", "Caox2");
        // 주어진 범죄 유형에 해당하는 데이터가 있는지 확인
        if (!crimeSumMap.containsKey(crimeType)) {
            return "보통"; // 해당 범죄 유형의 데이터가 없으면 범죄 상태를 LOW로 반환
        }

        // 해당 범죄 유형의 평균 값을 가져옴
        double avgCount = (double) CriminalLoader.GetCount(crimeType, time) / CriminalLoader.Avg().get(crimeType);
        System.out.println("CriminalLoader.GetCount(crimeType, time): " + CriminalLoader.GetCount(crimeType, time));
        System.out.println("CriminalLoader.Avg().get(crimeType): " + CriminalLoader.Avg().get(crimeType));
        System.out.println("avgCount: " + avgCount);
        System.out.println("-----------");
        // 시간대에 따른 범죄 발생을 확인하여 범죄 상태를 반환
        return calculateCrimeState(avgCount * new Average().get(crimeType,weekType));
    }


    private static String  calculateCrimeState(double avgCount) {
        if (avgCount <= 0.7)
            return "매우좋음";
        else if (avgCount <= 0.9)
            return "좋음";
        else if (avgCount <= 1.1)
            return "보통";
        else if (avgCount <= 1.3)
            return "나쁨";
        else
            return "매우나쁨";
    }

    public static String crimteExplain(String avgCount) {
        switch (avgCount) {
            case "매우좋음":
                return "오늘은 범죄 없는 날! 산책이라도 나가보는 건 어때요?";
            case "좋음":
                return "오늘은 범죄 상태 좋음! 오늘은 괜찮아요!";
            case "보통":
                return "오늘은 범죄 상태 보통. 주의하세요!";
            case "나쁨":
                return "오늘은 범죄 상태 나쁨. 밖을 조심하세요!";
            case "매우나쁨":
                return "오늘은 범죄 상태 매우 나쁨. 꼭 필요한 일이 아니면 외출을 삼가세요!";
            default:
                return "ERROR";
        }
    }

}
