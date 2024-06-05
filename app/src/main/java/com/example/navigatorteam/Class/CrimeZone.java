package com.example.navigatorteam.Class;

import com.example.navigatorteam.Support.CrimeType;

public class CrimeZone {

    public String GetImgLink()
    {
        return "default_Link" + crimeType.name();
    }

    public double lat;
    public double lon;
    public double radius;
    public CrimeType crimeType;
    public int grade;

    public CrimeZone() {}

    public CrimeZone(double lat, double lon, double radius, CrimeType crimeType, int grade) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.crimeType = crimeType;
        this.grade = grade;
    }


}
