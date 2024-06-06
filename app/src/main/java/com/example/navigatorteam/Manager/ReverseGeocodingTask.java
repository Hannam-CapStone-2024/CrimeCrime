package com.example.navigatorteam.Manager;

import android.os.AsyncTask;
import android.util.Log;

import com.example.navigatorteam.Support.ActivityManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReverseGeocodingTask extends AsyncTask<Void, Void, String> {
    private static final String API_KEY = "pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0";
    private String apiUrl;

    private double latitude;
    private double longitude;

    public ReverseGeocodingTask(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.apiUrl = String.format("https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&lat=%f&lon=%f&coordType=WGS84GEO&addressType=A10&coordYn=Y&keyInfo=Y&newAddressExtend=Y", latitude, longitude);
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Accept", "application/json");
            httpConn.setRequestProperty("appKey", API_KEY);

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("SSi", "result: " + result);
        // 결과를 UI에 반영
        result = extractAddress(result);
        if (result != null) {
            ActivityManager.getInstance().setDateTimeAndLocation(result);
            try {
                ActivityManager.getInstance().setCrimeStatus(result);
                ActivityManager.getInstance().setExplainText(result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Log.d("SSi", "convertLocationToAddress: " + result);
        } else {
            Log.d("SSi", "convertLocationToAddress: " + "Failed to get response");
        }
    }

    public static String extractAddress(String jsonResponse) {
        try {
            Log.d("SSi", "fullAddress: " + jsonResponse);
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject addressInfo = jsonObject.getJSONObject("addressInfo");
            String fullAddress = addressInfo.getString("fullAddress");
            Log.d("SSi", "fullAddress: " + fullAddress);
            // "서울특별시 중구 소공동" 부분을 추출
            String[] addressParts = fullAddress.split(",");
            if (addressParts.length > 0) {
                return addressParts[0];
            } else {
                return "Address not found";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing JSON";
        }
    }
}
