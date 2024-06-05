package com.example.navigatorteam.Support;

import android.content.Context;
import java.io.*;
import java.util.*;

public class CsvLoader {
    public static List<List<String>> readCSV(Context context, String fileName) {
        List<List<String>> csvList = new ArrayList<List<String>>();
        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
            while ((line = br.readLine()) != null) {
                List<String> aLine = new ArrayList<String>();
                String[] lineArr = line.split(",");
                aLine = Arrays.asList(lineArr);
                csvList.add(aLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close(); // 사용 후 BufferedReader를 닫아준다.
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return csvList;
    }
}
