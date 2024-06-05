package com.example.navigatorteam.Support;

import android.content.Context;

public enum CrimeType {
    Robbery("Robbery"), //강도
    Murder("Murder"), //살인
    Sexual_Violence("Sexual_Violence"), //성폭력
    Violence("Violence"), //폭력
    Etc("Etc"), //기타형법
    Moral("Moral"), //풍속범
    Intelli("Intelli"), //지능범
    Violent("Violent"), //강력범
    Theft("Theft"), //절도범
    Special("Special"),
    None("None");

    //특별범법
    private String value;

    CrimeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CrimeType fromValue(String value) {
        for (CrimeType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return CrimeType.None;
    }


    public static int getIconResourceByCrimeType(CrimeType crimeType, Context context) {
        int resourceId = 0;
        switch (crimeType) {
            case Robbery:
                resourceId = context.getResources().getIdentifier("robbery", "drawable", context.getPackageName());
                break;
            case Murder:
                resourceId = context.getResources().getIdentifier("murder", "drawable", context.getPackageName());
                break;
            case Sexual_Violence:
                resourceId = context.getResources().getIdentifier("sexual_violence", "drawable", context.getPackageName());
                break;
            case Violence:
                resourceId = context.getResources().getIdentifier("violence", "drawable", context.getPackageName());
                break;
            case Etc:
            case Moral:
            case Special:
            case None:
                resourceId = context.getResources().getIdentifier("etc", "drawable", context.getPackageName());
                break;
            case Intelli:
                resourceId = context.getResources().getIdentifier("intelli", "drawable", context.getPackageName());
                break;
            case Violent:
                resourceId = context.getResources().getIdentifier("violent", "drawable", context.getPackageName());
                break;
            case Theft:
                resourceId = context.getResources().getIdentifier("theft", "drawable", context.getPackageName());
                break;
        }
        return resourceId;
    }


}