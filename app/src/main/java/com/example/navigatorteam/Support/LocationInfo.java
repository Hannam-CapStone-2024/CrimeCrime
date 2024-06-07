package com.example.navigatorteam.Support;

import android.location.Location;
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
        // 중심 좌표
        Location centerLocation = new Location("center");
        centerLocation.setLatitude(centerY);
        centerLocation.setLongitude(centerX);

        // 점 좌표
        Location pointLocation = new Location("point");
        pointLocation.setLatitude(pointY);
        pointLocation.setLongitude(pointX);

        // 두 지점 간의 거리 계산 (미터 단위)
        float distance = centerLocation.distanceTo(pointLocation);
        Log.d("TAG", "distance: " + distance);
        Log.d("TAG", "radius: " + radius);
        // 거리와 반지름 비교
        return distance <= radius;
    }
}
