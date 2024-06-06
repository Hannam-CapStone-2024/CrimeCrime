package com.example.navigatorteam.Manager;

import android.content.Context;
import android.util.Log;

import com.example.navigatorteam.Class.Spot;
import com.example.navigatorteam.Support.CsvLoader;

import java.util.ArrayList;
import java.util.List;

public class SpotController {
    private static ArrayList<Spot> spots = new ArrayList<>();

    private SpotController() { }


    public static void init(Context context) {
        List<List<String>> csvLoader = CsvLoader.readCSV(context, "DB/SpotDB.csv");
        spots.removeAll(spots);
        for (List<String> arr : csvLoader) {
            Log.d("init: ", "initss: " + arr);
            Spot spot = parseSpot(arr.toArray(new String[0]));
            spots.add(spot);
        }
    }

    private static Spot parseSpot(String[] arr) {
        String type = arr[0];
        double latitude = Double.parseDouble(arr[1].trim().replace("\uFEFF", ""));
        double longitude = Double.parseDouble(arr[2].trim());
        String name = arr[3];
        String exp = arr[4];
        return new Spot(type, latitude, longitude, name, exp);
    }

    public static ArrayList<Spot> getSpots() {
        return spots;
    }
}
