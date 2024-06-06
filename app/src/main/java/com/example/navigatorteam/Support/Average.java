package com.example.navigatorteam.Support;

import android.util.Log;

import com.example.navigatorteam.Class.WeekType;
import com.example.navigatorteam.MainActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Average {

    static Map<CrimeType, Map<WeekType, Double>> crimeData = new HashMap<>();

    public static void Init()
    {
        crimeDataArray = CsvLoader.readCSV(MainActivity.Instance,"DB/CrimeDays.csv");
        for (List<String> row : crimeDataArray) {
            CrimeType crimeType = CrimeType.fromValue(row.get(0));
            Map<WeekType, Double> dayData = new HashMap<>();
            for (int i = 1; i < row.size(); i++) {
                dayData.put(WeekType.values()[i - 1], Double.parseDouble(row.get(i).trim()));
            }
            crimeData.put(crimeType, dayData);
        }
    }

    static List<List<String>> crimeDataArray;

    public static double get(CrimeType crimeType, WeekType day) {
        if (!crimeData.containsKey(crimeType)) {
            return 1; // Invalid crime type
        }

        Map<WeekType, Double> dayData = crimeData.get(crimeType);
        if (!dayData.containsKey(day)) {
            return 1; // Invalid day of the week
        }

        double sum = 0;
        for (WeekType d : WeekType.values()) {
            sum += dayData.get(d);
        }
        double average = sum / WeekType.values().length;
        double value = dayData.get(day);
        return value / average;
    }
}
