package com.example.navigatorteam.Class;

public class Spot {
    private double lat;
    private double lon;
    private String name;
    private String explain;
    private String type;

    // Constructor
    public Spot(String Type,double lat, double lon, String name, String explain) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.explain = explain;
        this.type = Type;  // Default value for type, can be updated later
    }

    // Getter and Setter methods
    public  String getSpotType() { return type; }
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
