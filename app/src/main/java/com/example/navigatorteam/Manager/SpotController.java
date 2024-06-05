package com.example.navigatorteam.Manager;

import android.content.Context;

import com.example.navigatorteam.Class.Spot;
import com.example.navigatorteam.Support.CsvLoader;

import java.util.ArrayList;
import java.util.List;

public class SpotController {
    private static SpotController instance;
    private ArrayList<Spot> spots = new ArrayList<>();

    private SpotController() { }

    public static SpotController getInstance() {
        if (instance == null) {
            instance = new SpotController();
        }
        return instance;
    }

    public void init(Context context) {
        List<List<String>> csvLoader = CsvLoader.readCSV(context, "DB/SpotDB.csv");
        spots.removeAll(spots);
        for (List<String> arr : csvLoader) {
            Spot spot = parseSpot(arr.toArray(new String[0]));
            spots.add(spot);
        }
    }

    private Spot parseSpot(String[] arr) {
        String type = arr[0];
        double latitude = Double.parseDouble(arr[1].trim().replace("\uFEFF", ""));
        double longitude = Double.parseDouble(arr[2].trim());
        String name = arr[3];
        String exp = arr[4];
        return new Spot(type, latitude, longitude, name, exp);
    }

    public ArrayList<Spot> getSpots() {
        return spots;
    }
}
