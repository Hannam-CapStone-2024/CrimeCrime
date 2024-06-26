package com.example.navigatorteam.Manager;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.navigatorteam.Class.CrimeRecord;
import com.example.navigatorteam.Class.TimeRange;
import com.example.navigatorteam.Support.CrimeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CriminalLoader {

    public static Map<CrimeType, Integer> avgCount = new HashMap<>();


    public static List<CrimeRecord> Data() throws IOException {
        return convertJsonToCrimeRecords(readFileAsString("CrimeData.json"));
    }

    public static Map<CrimeType, CrimeRecord> Sum() throws IOException
    {
        List<CrimeRecord> dataArray = Data();
        Log.v("Caox File is :" , dataArray.toString());
        Map<CrimeType, CrimeRecord> crimeRecordMap = new HashMap<>();
        for (int i = 0; i < dataArray.size(); i++) {
            CrimeRecord it = dataArray.get(i);

            // 기존에 같은 범죄 유형의 기록이 있다면 Count를 누적, 없으면 새로운 CrimeRecord 생성
            if (crimeRecordMap.containsKey(it.category)) {
                CrimeRecord existingRecord = crimeRecordMap.get(it.category);
                existingRecord.night_20_24 += it.night_20_24 / 2;
                existingRecord.earlyMorning_4_7 += it.earlyMorning_4_7 / 2;
                existingRecord.midnight_0_4 += it.midnight_0_4 / 2;
                existingRecord.morning_7_12 += it.morning_7_12 / 2;
                existingRecord.afternoon_12_18 += it.afternoon_12_18 / 2;
                existingRecord.evening_18_20 += it.evening_18_20 / 2;

            } else {
                crimeRecordMap.put(it.category, it);
            }
        }
        return crimeRecordMap;
    }

    public static Map<CrimeType, Integer> Avg() throws IOException
    {
        List<CrimeRecord> dataArray = Data();
        Map<CrimeType, Integer> crimeSumMap = new HashMap<>();
        Log.v("Caox File is :" , dataArray.toString());
        for (CrimeRecord it : dataArray) {
            int total = (it.night_20_24 + it.earlyMorning_4_7 + it.midnight_0_4 +
                    it.morning_7_12 + it.afternoon_12_18 + it.evening_18_20) / 6;
            crimeSumMap.merge(it.category, total, Integer::sum);
        }
        return crimeSumMap;
    }

    public static int GetCount(CrimeType crimeType, TimeRange timeRange) {
        List<CrimeRecord> crimeRecords;
        try {
            crimeRecords = Data(); // 데이터를 읽어옴
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        // 주어진 범죄 유형과 시간대에 해당하는 범죄 발생 횟수를 계산
        int count = 0;
        for (CrimeRecord record : crimeRecords) {
            if (record.category == crimeType) {
                switch (timeRange) {
                    case MIDNIGHT_0_4:
                        count += record.midnight_0_4;
                        break;
                    case EARLY_MORNING_4_7:
                        count += record.earlyMorning_4_7;
                        break;
                    case MORNING_7_12:
                        count += record.morning_7_12;
                        break;
                    case AFTERNOON_12_18:
                        count += record.afternoon_12_18;
                        break;
                    case EVENING_18_20:
                        count += record.evening_18_20;
                        break;
                    case NIGHT_20_24:
                        count += record.night_20_24;
                        break;
                }
            }
        }

        return count;
    }

    private static Context context;

    public CriminalLoader(Context context) {
        this.context = context;
    }
    public static String readFileAsString(String filePath) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(filePath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static List<CrimeRecord> convertJsonToCrimeRecords(String jsonString) {
        List<CrimeRecord> crimeRecords = new ArrayList<>();

        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        JsonArray dataArray = jsonObject.getAsJsonArray("data");

        for (int i = 0; i < dataArray.size(); i++) {
            JsonObject dataObject = dataArray.get(i).getAsJsonObject();

            int night_20_24 = dataObject.get("밤(20시-24시)").getAsInt();
            int earlyMorning_4_7 = dataObject.get("새     벽(4시-7시)").getAsInt();
            int midnight_0_4 = dataObject.get("심     야(0시-4시)").getAsInt();
            int year = dataObject.get("연도").getAsInt();
            int morning_7_12 = dataObject.get("오     전(7시-12시)").getAsInt();
            int afternoon_12_18 = dataObject.get("오     후(12시-18시)").getAsInt();
            int evening_18_20 = dataObject.get("초 저 녁(18시-20시)").getAsInt();
            CrimeType category = CrimeType.valueOf(translate(dataObject.get("항목").getAsString()));

            CrimeRecord crimeRecord = new CrimeRecord(night_20_24, earlyMorning_4_7, midnight_0_4, year,
                    morning_7_12, afternoon_12_18, evening_18_20, category);
            crimeRecords.add(crimeRecord);
        }

        return crimeRecords;
    }

    private static final Map<String, String> translationMap = new HashMap<>();
    static {
        translationMap.put("강도", "Robbery");
        translationMap.put("살인", "Murder");
        translationMap.put("성폭력", "Sexual_Violence");
        translationMap.put("폭력범", "Violence");
        translationMap.put("기타형법범", "Etc");
        translationMap.put("풍속범", "Moral");
        translationMap.put("지능범", "Intelli");
        translationMap.put("강력범", "Violent");
        translationMap.put("절도범", "Theft");
        translationMap.put("특별법범", "Special");
        translationMap.put("없음", "None");
    }

    public static String translate(String koreanCrimeType) {
        return translationMap.getOrDefault(koreanCrimeType, "None");
    }

    private static void updateHighestCount(CrimeRecord record) {
        // 현재 레코드의 각 시간대 값 중 가장 큰 값을 highestCount 맵에 업데이트
        updateMaxCount(record.category, record.night_20_24);
        updateMaxCount(record.category, record.earlyMorning_4_7);
        updateMaxCount(record.category, record.midnight_0_4);
        updateMaxCount(record.category, record.morning_7_12);
        updateMaxCount(record.category, record.afternoon_12_18);
        updateMaxCount(record.category, record.evening_18_20);
    }

    private static void updateMaxCount(CrimeType category, int value) {
        if (avgCount.containsKey(category)) {
            int currentMax = avgCount.get(category);
            avgCount.put(category, Math.max(currentMax, value));
        } else {
            avgCount.put(category, value);
        }
    }
}
