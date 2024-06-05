package com.example.navigatorteam.Support;

import android.util.Log;

import com.example.navigatorteam.Class.CrimeZone;
import com.example.navigatorteam.CrimeZoneController;

public class LocationInfo {
    public static boolean isDanger(double lon, double lat) {

        if (getDangerCrimeZone(lon, lat) != null) {
            return true;
        }
        return false;
    }

    public static String DangerGrade(double lon, double lat) {
        CrimeZone dangerZone = getDangerCrimeZone(lon, lat);
        if (dangerZone != null) {
            return Integer.toString(dangerZone.grade);
        }
        return "N/A"; // 위험 구역이 없는 경우 처리
    }

    // NowPos is String Type : "lon,lat"
    static CrimeZone getDangerCrimeZone(double lon, double lat) {
        for (CrimeZone it : CrimeZoneController.GetInstance().GetCrimeZones()) {
            if (isPointInsideCircle(it.lon, it.lat, it.radius, lon, lat)) {
                Log.d("TAG", "getDangerCrimeZone: " + it.lon + ',' + it.lat + ',' + lon+ ',' + lat);
                return it;
            }
        }
        return null;
    }

    public static boolean isPointInsideCircle(double centerX, double centerY, double radius, double pointX, double pointY) {
        double distance = Math.sqrt(Math.pow((centerX - pointX), 2) + Math.pow((centerY - pointY), 2));
        Log.d("TAG", "distance: " + distance);
        Log.d("TAG", "distance-radius: " + radius);
        return distance <= radius;
    }
}
