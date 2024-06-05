package com.example.navigatorteam.Manager;

import android.util.Log;

import com.example.navigatorteam.MainActivity;
import com.example.navigatorteam.Support.CsvLoader;

import java.util.List;

public class RegionManager {
    public static double GetValue(String str) {

        Log.d("GetValue: ", "Find: " + Find(str) );
        Log.d("GetValue: ", " Avg(): " +  Avg());
        return Find(str) / Avg();
    }


    // 특정 문자열을 검색하여 해당 값을 반환하는 메서드
    public static double Find(String str) {
        // CSV 파일에서 데이터를 읽어옴
        List<List<String>> csvLoader = CsvLoader.readCSV(MainActivity.Instance, "DB/Region.csv");

        if (str == null || str.isEmpty()) {
            Log.e("CsvDataFinder", "검색 문자열이 null이거나 비어있습니다.");
            return Avg();
        }

        // CSV 파일의 각 행을 순회하면서 검색
        for (List<String> row : csvLoader) {
            Log.d("CsvDataFinder", "Find: " + row.get(0));
            if (row != null && row.size() >= 2 && str.contains(row.get(0))) {
                try {
                    // 문자열에 해당하는 값을 double로 변환하여 반환
                    return Double.parseDouble(row.get(1));
                } catch (NumberFormatException e) {
                    // 값이 숫자로 변환할 수 없는 경우 예외 처리
                    e.printStackTrace();
                    return  Avg(); // 오류를 나타내는 값으로 -1 반환 (필요에 따라 변경 가능)
                }
            }
        }
        // 문자열이 검색되지 않은 경우 -1 반환
        return  Avg();
    }


    public static double Avg() {
        // CSV 파일에서 데이터를 읽어옴
        List<List<String>> csvLoader = CsvLoader.readCSV(MainActivity.Instance, "DB/SpotDB.csv");

        double sum = 0;
        int count = 0;

        // CSV 파일의 각 행을 순회하면서 값을 합산하고 개수를 셈
        for (List<String> row : csvLoader) {
            if (row.size() >= 2) {
                try {
                    double value = Double.parseDouble(row.get(1));
                    sum += value;
                    count++;
                } catch (NumberFormatException e) {
                    // 값이 숫자로 변환할 수 없는 경우 예외 처리
                    e.printStackTrace();
                }
            }
        }

        // 평균 계산 및 반환 (값이 없는 경우 0 반환)
        return count > 0 ? sum / count : 1;
    }
}
