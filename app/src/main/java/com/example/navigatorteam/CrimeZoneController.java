package com.example.navigatorteam;

import android.util.Log;

import com.example.navigatorteam.Class.CrimeZone;
import com.example.navigatorteam.Support.CrimeType;
import com.example.navigatorteam.Support.CsvLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class CrimeZoneController
{
    public String GetCrimeZone(String crimeType) {
        for (CrimeZone zone : GetCrimeZones()) {
            System.out.println("Latitude: " + zone.lon);
            System.out.println("Longitude: " + zone.lat);
            System.out.println("Radius: " + zone.radius);
            System.out.println("Name: " + zone.crimeType);
            System.out.println("Crime Level: " + zone.grade);
            System.out.println("------------------------");
        }


        ObjectMapper mapper = new ObjectMapper();
        String jsonResult;
        try {
            System.out.println("Output: " + CrimeType.fromValue(crimeType));
            jsonResult = mapper.writeValueAsString(GetCrimeZones(CrimeType.fromValue(crimeType)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            jsonResult = "{\"error\": \"Error processing crime zones\"}";
        }
        System.out.println("jsonResult: " + jsonResult);
        return jsonResult;
    }

    public String TestCrime() {
        return GetCrimeZones().toString();
    }

    public ArrayList<CrimeZone> GetCrimeZones()
    {
        ArrayList<CrimeZone> crimeZones = new ArrayList<>();
        List<List<String>> csvLoader = CsvLoader.readCSV(MainActivity.Instance,"DB/CrimePoint_within3km.csv");
        Log.d("AAAAACDE", "GetCrimeZones: " + csvLoader.toString());
        for (List<String> arr : csvLoader) {
            CrimeZone crimeZone = parseCrimeZone(arr.toArray(new String[0]));
            crimeZones.add(crimeZone);
        }
        return crimeZones;
    }

    private CrimeZone parseCrimeZone(String[] arr) {
        double latitude = Double.parseDouble(arr[0].trim().replace("\uFEFF", ""));
        double longitude = Double.parseDouble(arr[1].trim());
        double radius = Double.parseDouble(arr[2].trim());
        String name = arr[3];
        int crimeLevel = Integer.parseInt(arr[4].trim());
        return new CrimeZone(latitude, longitude, radius, CrimeType.fromValue(name), crimeLevel);
    }

    public ArrayList<CrimeZone> GetCrimeZones(CrimeType crimeType)
    {
        ArrayList<CrimeZone> it = new ArrayList<CrimeZone>();

        if(crimeType == CrimeType.None) return GetCrimeZones();

        for (CrimeZone zone : GetCrimeZones())
        {
            if(crimeType == zone.crimeType)
                it.add(zone);
        }
        return it;
    }

    static CrimeZoneController instance;

    public static CrimeZoneController GetInstance()
    {
        if(instance == null)
        {
            instance = new CrimeZoneController();
            instance.GetCrimeZones();
        }
        return instance;
    }

    public CrimeZoneController()
    {

    }
}
