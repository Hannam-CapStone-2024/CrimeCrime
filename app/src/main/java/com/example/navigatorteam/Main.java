package com.example.navigatorteam;

import static com.example.navigatorteam.Support.CrimeType.getIconResourceByCrimeType;

import android.Manifest;
import android.graphics.PointF;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navigatorteam.Class.CrimeZone;
import com.example.navigatorteam.Class.Spot;
import com.example.navigatorteam.Manager.SpotController;
import com.example.navigatorteam.Support.CrimeType;
import com.example.navigatorteam.databinding.ActivityMainBinding;
import com.skt.tmap.TMapAutoCompleteV2;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapGpsManager;
import com.skt.tmap.TMapInfo;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;
import com.skt.tmap.overlay.TMapCircle;
import com.skt.tmap.overlay.TMapMarkerItem;
import com.skt.tmap.overlay.TMapMarkerItem2;
import com.skt.tmap.overlay.TMapPolyLine;
import com.skt.tmap.poi.TMapPOIItem;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main extends AppCompatActivity implements LocationListener{
    private LocationManager locationManager;
    private ImageView zoomInImage;
    private int zoomIndex = -1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1.0f;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Location location;
    private Location mylocation;

    private LinearLayout routeLayout;
    private TextView routeDistanceTextView;
    private TextView routeTimeTextView;
    private TextView routedescriptionTextView;
    private ImageView zoomOutImage;
    private TextView zoomLevelTextView;
    private ActivityMainBinding binding;
    private LinearLayout autoCompleteLayout;
    private EditText autoCompleteEdit;
    private TMapView tMapView;
    private ListView autoCompleteListView;
    private TMapPoint nowpoint;

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "Location changed");
        runOnUiThread(() -> {
            try {
                nowpoint = new TMapPoint(location.getLatitude(), location.getLongitude());
            } catch (Exception e) {
                Log.e("onLocationChanged", "Error in location update", e);
            }
        });
    }

    private void checkLocationPermission() {
        // 위치 권한이 부여되어 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 사용자에게 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 부여되었으면 위치 정보 가져오기
            getLocation();
        }
    }

    private void getLocation() {
        // 위치 관리자 가져오기
        android.location.LocationManager locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            // GPS 또는 네트워크를 통해 마지막으로 알려진 위치 가져오기
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location = (gpsLocation != null) ? gpsLocation : networkLocation;

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                nowpoint = new TMapPoint(latitude,longitude);
            } else {
                // 마지막으로 알려진 위치가 없는 경우, 새로운 위치 업데이트를 요청합니다.
                // 이 부분은 필요에 따라 구현하실 수 있습니다.
                Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TMapView 초기화
        tMapView = new TMapView(this);
        binding.tmapViewContainer.addView(tMapView);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            checkLocationPermission();
        } else {
            Toast.makeText(this, "Location Manager를 초기화할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        // TMapView 설정
        tMapView.setSKTMapApiKey("pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0");
        tMapView.setOnClickListenerCallback(new TMapView.OnClickListenerCallback() {
            @Override
            public void onPressDown(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {

                if (tMapView.isTrackingMode()) {
                    setTrackingMode(false);
                    locationImage.setSelected(false);
                }

            }

            @Override
            public void onPressUp(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {

            }
        });
        // 맵 로딩 완료 리스너 설정
        tMapView.setOnMapReadyListener(new TMapView.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // 맵 로딩 완료 후 처리할 작업
                Log.d("MainActivity", "TMapView is ready");
                initAutoComplete();
                centerPositon();
                showAll();
                int zoom = tMapView.getZoomLevel();
                zoomLevelTextView.setText("Lv." + zoom);
            }

        });
        tMapView.setOnEnableScrollWithZoomLevelListener(new TMapView.OnEnableScrollWithZoomLevelCallback() {
            @Override
            public void onEnableScrollWidthZoomLevel(float v, TMapPoint tMapPoint) {
                int zoom = (int) v;
                zoomLevelTextView.setText("Lv." + zoom);
                zoomIndex = zoom - 6;
            }
        });

        tMapView.setOnCallOutRightButtonClickListener(new TMapView.OnCallOutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                ReverseLabelView dialog = new ReverseLabelView(Main.this, false);
                dialog.setText(name + " \n("+ bizCatName + ") ", newAddress + " " +na1,tel, homepage );
                dialog.show();
            }
        });

        zoomInImage = findViewById(R.id.zoomInImage);
        zoomInImage.setOnClickListener(onClickListener);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        zoomOutImage.setOnClickListener(onClickListener);
        locationImage = findViewById(R.id.locationImage);
        locationImage.setOnClickListener(onClickListener);

        // 어댑터 초기화

        // 뷰 초기화
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        zoomLevelTextView = findViewById(R.id.zoomLevelText);
        routeLayout = findViewById(R.id.routeLayout);
        routeDistanceTextView = findViewById(R.id.routeDistanceText);
        routeTimeTextView = findViewById(R.id.routeTimeText);
        routedescriptionTextView = findViewById(R.id.routedescriptionTextView);


        ImageView policeButton = findViewById(R.id.poLice);

        policeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nearpolice("파출소 지구대");
            }
        });

        // 어댑터 설정

        Button buttonWalkingRoute = findViewById(R.id.buttonWalkingRoute);
        buttonWalkingRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, WalkingRouteActivity.class);
                startActivity(intent);
            }
        });

        Button buttonHome = findViewById(R.id.buttonHome);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, MainActivity.class);
                startActivity(intent);
            }
        });


        // SpotController 초기화

        Button buttonShowAll = findViewById(R.id.buttonShowAll);
        Button buttonShowFacilities = findViewById(R.id.buttonShowFacilities);
        Button buttonShowCrimes = findViewById(R.id.buttonShowCrimes);
        Button buttonHideAll = findViewById(R.id.buttonHideAll);

        buttonShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAll();
            }
        });

        buttonShowFacilities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFacilities();
            }
        });

        buttonShowCrimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCrimes();
            }
        });

        buttonHideAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAll();
            }
        });
    }
    private void nearpolice(String data) {
        TMapData tMapData = new TMapData();
        tMapData.findAroundNamePOI(nowpoint, data, 2, 1, new TMapData.OnFindAroundNamePOIListener() {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> arrayList) {
                tMapView.removeAllTMapMarkerItem();
                ArrayList<TMapPoint> pointList = new ArrayList<>();
                if (arrayList != null) {
                    for (TMapPOIItem item : arrayList) {
                        // 각 POI에 대해 상세 정보 가져오기
                        getPOIDetail(item);
                        // 나머지 코드는 유지됨
                        tMapView.addTMapPOIItem(arrayList);
                        pointList.add(item.getPOIPoint());
                    }
                    TMapInfo info = tMapView.getDisplayTMapInfo(pointList);
                    tMapView.setZoomLevel(info.getZoom());
                    tMapView.setCenterPoint(info.getPoint().getLatitude(), info.getPoint().getLongitude());
                }
            }
        });
    }
    private void updateZoomLevel(boolean upanddown) {
        if(upanddown == true)
        {
            int zoom = tMapView.getZoomLevel();
            int zoomup = zoom + 1;
            zoomLevelTextView.setText("Lv." + zoomup);
            if(zoomup == 20)
            {
                zoomup = 19;
                zoomLevelTextView.setText("Lv." + zoomup);
            }
        }
        else
        {
            int zoom = tMapView.getZoomLevel();
            int zoomdown = zoom - 1;
            zoomLevelTextView.setText("Lv." + zoomdown);
        }
    }
    private AutoComplete2ListAdapter autoComplete2ListAdapter;
    private void initAutoComplete() {
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteLayout.setVisibility(View.VISIBLE);
        autoCompleteEdit = findViewById(R.id.autoCompleteEdit);
        autoCompleteListView = findViewById(R.id.autoCompleteListView);
        autoComplete2ListAdapter = new AutoComplete2ListAdapter(this);
        autoCompleteListView.setAdapter(autoComplete2ListAdapter);

        autoCompleteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TMapAutoCompleteV2 item = (TMapAutoCompleteV2) autoComplete2ListAdapter.getItem(position);
                if (item != null) {
                    String keyword = item.keyword;
                    findAllPoi(keyword);
                    autoCompleteListView.setVisibility(View.GONE);
                }
            }
        });

        autoCompleteEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                TMapData tMapData = new TMapData();
                Log.d("TAG", "출발지 입력 전 내 현재위치: " + nowpoint);
                Log.d("TAG", "검색 반경: " + 1000); // 검색 반경 로그 출력
                tMapData.autoCompleteV2(keyword, nowpoint.getLatitude(), nowpoint.getLongitude(), 1000, 10, new TMapData.OnAutoCompleteV2Listener() {
                    @Override
                    public void onAutoCompleteV2(ArrayList<TMapAutoCompleteV2> arrayList) {
                        ArrayList<TMapAutoCompleteV2> filteredList = new ArrayList<>();
                        for (TMapAutoCompleteV2 item : arrayList) {
                            try {
                                double itemLat = Double.parseDouble(item.lat);
                                double itemLon = Double.parseDouble(item.lon);
                                double distance = calculateDistance2(nowpoint.getLatitude(), nowpoint.getLongitude(), itemLat, itemLon);
                                if (distance <= 2000) { // 1km 이내의 POI만 필터링
                                    filteredList.add(item);
                                }
                            } catch (NumberFormatException e) {
                                Log.e("TAG", "Invalid latitude or longitude format: " + item.lat + ", " + item.lon, e);
                            }
                        }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    autoCompleteListView.setVisibility(View.VISIBLE);
                                    autoComplete2ListAdapter.setItemList(arrayList);
                                }
                            });
                        }
                    });
                }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        autoCompleteEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String keyword = autoCompleteEdit.getText().toString();
                    if (!keyword.isEmpty()) {
                        findAllPoi(keyword);
                        autoCompleteListView.setVisibility(View.GONE);
                    }
                    return true;
                }
                return false;
            }
        });
    }
    public void findAllPoi(String strData) {
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(strData, new TMapData.OnFindAllPOIListener() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItemList) {
                ArrayList<TMapPOIItem> filteredPoiList = filterPoiList(poiItemList, strData);
                showPOIResultDialog(filteredPoiList, mylocation);
            }
        });
    }

    private ArrayList<TMapPOIItem> filterPoiList(ArrayList<TMapPOIItem> poiItemList, String keyword) {
        ArrayList<TMapPOIItem> filteredList = new ArrayList<>();
        for (TMapPOIItem poiItem : poiItemList) {
            if (poiItem.getPOIName().equalsIgnoreCase(keyword)) {
                filteredList.add(poiItem);
            }
        }
        return filteredList;
    }

    private void showPOIResultDialog(final ArrayList<TMapPOIItem> poiItem, final Location myLocation) {
        if (poiItem != null) {
            // 현재 위치를 기준으로 POI 리스트를 거리순으로 정렬
            Collections.sort(poiItem, new Comparator<TMapPOIItem>() {
                @Override
                public int compare(TMapPOIItem poi1, TMapPOIItem poi2) {
                    double distance1 = calculateDistance2(nowpoint.getLatitude(), nowpoint.getLongitude(), poi1.getPOIPoint().getLatitude(), poi1.getPOIPoint().getLongitude());
                    double distance2 = calculateDistance2(nowpoint.getLatitude(), nowpoint.getLongitude(), poi2.getPOIPoint().getLatitude(), poi2.getPOIPoint().getLongitude());

                    // 디버깅을 위해 거리 출력
                    Log.d("DistanceDebug", "POI 1: " + poi1.getPOIName() + " Distance: " + distance1);
                    Log.d("DistanceDebug", "POI 2: " + poi2.getPOIName() + " Distance: " + distance2);
                    Log.d("NP", "현재 위치: " + nowpoint + " " + nowpoint.getLatitude() + " " + nowpoint.getLongitude());
                    return Double.compare(distance1, distance2);
                }
            });
            for (TMapPOIItem item : poiItem) {
                double distance = calculateDistance2(nowpoint.getLatitude(), nowpoint.getLongitude(), item.getPOIPoint().getLatitude(), item.getPOIPoint().getLongitude());
                Log.d("SortedPOI", "POI: " + item.getPOIName() + " Distance: " + distance);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CharSequence[] item = new CharSequence[poiItem.size()];
                    for (int i = 0; i < poiItem.size(); i++) {
                        item[i] = poiItem.get(i).getPOIName() + " (" +  (int) calculateDistance2(nowpoint.getLatitude(), nowpoint.getLongitude(), poiItem.get(i).getPOIPoint().getLatitude(), poiItem.get(i).getPOIPoint().getLongitude()) + "m)";
                    }
                    new AlertDialog.Builder(Main.this)
                            .setTitle("검색 결과")
                            .setIcon(R.drawable.black_sc)
                            .setItems(item, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    initAll();
                                    TMapPOIItem poi = poiItem.get(i);
                                    getPOIDetail(poi);
                                    autoCompleteLayout.setVisibility(View.VISIBLE);
                                    autoCompleteEdit.setText("");
                                    autoCompleteListView.setVisibility(View.GONE);
                                    Log.d("TAG", "포이 보냈음.: " + poi.getPOIName());
                                }
                            }).create().show();
                }
            });
        }
    }

    // 두 지점 간 거리 계산 (Haversine 공식 사용)
    private double calculateDistance2(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (단위: km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 단위: m
        return distance;
    }
    private void getPOIDetail(TMapPOIItem poi) {
        // POI 상세 정보 요청
        String poiInfo = poi.id; // POI ID를 사용하여 상세 정보 요청
        Log.d("TAG", "POIID: " + poi.id);
        String version = "1";
        String resCoordType = "WGS84GEO"; // 응답 좌표 타입 설정 (예시: WGS84GEO)
        String callback = ""; // 콜백 값 설정 (필요시)
        String appKey = "pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0"; // 사용자의 APP_KEY 입력

        String requestUrl = "https://apis.openapi.sk.com/tmap/pois/" + poiInfo + "?version=" + version + "&resCoordType=" + resCoordType + "&callback=" + callback + "&appKey=" + appKey;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = getPOIDetailInfo(requestUrl);
                    Log.d("TAG", "제이슨 가져옴.: ");
                    if (jsonObject != null) {
                        parsePOIDetail(jsonObject); // 수정된 부분
                    }
                } catch (Exception e) {
                    Log.d("TAG", "제이슨 못가져옴.: ");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private JSONObject getPOIDetailInfo(String requestUrl) throws IOException, JSONException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 응답 처리
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        Log.d("TAG", "응답 가져옴.: ");
        // 응답 결과를 처리하여 JSON 객체로 반환
        return new JSONObject(response.toString());

    }
    private String viewId;
    private String id;
    private String dbKind;
    private String pkey;
    private String navSeq;
    private String name;
    private String bizCatName;
    private String newAddress;
    private String na1;
    private String tel;
    private double latitude;
    private double longitude;
    private String homepage;
    private void parsePOIDetail(JSONObject jsonObject) {
        try {
            JSONObject poiDetailInfo = jsonObject.getJSONObject("poiDetailInfo");
            if (poiDetailInfo != null) {
                viewId = poiDetailInfo.optString("viewId", "");
                id = poiDetailInfo.optString("id", "");
                dbKind = poiDetailInfo.optString("dbKind", "");
                pkey = poiDetailInfo.optString("pkey", "");
                navSeq = poiDetailInfo.optString("navSeq", "");
                name = poiDetailInfo.optString("name", "");
                bizCatName = poiDetailInfo.optString("bizCatName", "");
                newAddress = poiDetailInfo.optString("bldAddr", "");
                na1 = poiDetailInfo.optString("bldNo1", "");
                tel = poiDetailInfo.optString("tel", "");
                latitude = poiDetailInfo.optDouble("lat", 0.0); // 위도 정보 가져오기
                longitude = poiDetailInfo.optDouble("lon", 0.0); // 경도 정보 가져오기
                homepage = poiDetailInfo.optString("homepageURL", "");

                TMapPoint tMapPoint = new TMapPoint(latitude,longitude);

                // 추출한 정보를 사용하거나 표시하는 등의 작업을 수행할 수 있습니다.
                // 예: Log로 출력
                Log.d("POI Detail", "View ID: " + viewId);
                Log.d("POI Detail", "ID: " + id);
                Log.d("POI Detail", "DB Kind: " + dbKind);
                Log.d("POI Detail", "PKey: " + pkey);
                Log.d("POI Detail", "Nav Seq: " + navSeq);
                Log.d("POI Detail", "Name: " + name);
                Log.d("POI Detail", "Business Category Name: " + bizCatName);

                TMapMarkerItem marker = new TMapMarkerItem();
                marker.setId(id);
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_searchlist);
                marker.setIcon(icon);
                marker.setCanShowCallout(true);
                marker.setCalloutTitle(name);
                marker.setCalloutSubTitle(newAddress + na1);
                marker.setTMapPoint(latitude,longitude);
                marker.setCalloutRightButtonImage(bitmap);
                tMapView.addTMapMarkerItem(marker);
                tMapView.setCenterPoint(latitude,longitude);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void initAll() {

        tMapView.removeAllTMapMarkerItem2();
        tMapView.removeAllTMapMarkerItem();
        tMapView.removeAllTMapPolyLine();
        tMapView.removeAllTMapPolygon();
        tMapView.removeAllTMapCircle();
        tMapView.removeAllTMapPOIItem();
        tMapView.removeAllTMapOverlay();
        tMapView.removeTMapPath();
        tMapView.setPOIScale(TMapView.POIScale.NORMAL);
    }
    public void addMarkersAndCircles(List<CrimeZone> crimeZones) {

        Log.d("TAG", "addMarkersAndCircles() returned: " + crimeZones);
        int i = 0;
        for (CrimeZone crimeZone : crimeZones) {
            TMapPoint lonlat = new TMapPoint(crimeZone.lat, crimeZone.lon);
            String title = "zone";
            double radius = crimeZone.radius;
            int crimeLevel = crimeZone.grade;

            // 범죄 수준에 따라 투명도 계산
            int opacity = (int)((crimeLevel / 5f) * 255); // 필요에 따라 이 계산을 조정합니다.

            // 마커 객체 생성
            TMapMarkerItem markerItem = new TMapMarkerItem();
            markerItem.setId("tmp_" + i);
            markerItem.setTMapPoint(lonlat);
            markerItem.setName(title);
            markerItem.setVisible(markerItem.getVisible());;
            Bitmap icon = BitmapFactory.decodeResource(getResources(), CrimeType.getIconResourceByCrimeType(crimeZone.crimeType, Main.this));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(icon, 64, 64, false);
            markerItem.setIcon(scaledBitmap);
            tMapView.addTMapMarkerItem(markerItem);

            // 반경을 원으로 표시
            TMapCircle circle = new TMapCircle();
            circle.setId("tmp_circle_"+ i);
            circle.setCenterPoint(lonlat);
            circle.setRadius(radius);
            circle.setAreaColor(Color.RED); // 투명도 동적으로 설정
            circle.setAreaAlpha(opacity);
            circle.setLineAlpha(opacity);
            circle.setLineColor(Color.RED);
            circle.setCircleWidth(5);
            circle.setRadiusVisible(true);
            tMapView.addTMapCircle(circle);
            i++;
        }

        // 첫 번째 마커를 중심으로 지도를 설정
        if (!crimeZones.isEmpty()) {
            CrimeZone firstZone = crimeZones.get(0);
            tMapView.setCenterPoint(firstZone.lon, firstZone.lat);
        }
    }

    // Add spots to the map
    private void addSpots(List<Spot> spots) {
        int i = 0;
        for (Spot spot : spots) {
            TMapPoint point = new TMapPoint(spot.getLat(), spot.getLon());
            TMapMarkerItem marker = new TMapMarkerItem();
            marker.setId("Spot" + i);
            marker.setTMapPoint(point);
            marker.setCanShowCallout(true);
            marker.setCalloutTitle(spot.getName());
            marker.setCalloutSubTitle(spot.getExplain());
            Bitmap icon = (spot.getSpotType().equalsIgnoreCase("crime")) ? BitmapFactory.decodeResource(getResources(), R.drawable.spot_icon_1) : BitmapFactory.decodeResource(getResources(), R.drawable.spot_icon_2);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(icon, 64, 64, false);
            marker.setIcon(scaledBitmap);
            tMapView.addTMapMarkerItem(marker);
            i++;
        }
    }

    private void showAll() {
        hideAll();
        addMarkersAndCircles(CrimeZoneController.GetInstance().GetCrimeZones());
        addSpots(SpotController.getSpots());
    }

    private void showFacilities() {
        hideAll();
        addSpots(SpotController.getSpots());
    }

    private void showCrimes() {
        hideAll();
        addMarkersAndCircles(CrimeZoneController.GetInstance().GetCrimeZones());
    }

    private void hideAll() {
        tMapView.removeAllTMapMarkerItem();
        tMapView.removeAllTMapCircle();
    }
    private ImageView locationImage;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(zoomInImage)) {
                tMapView.mapZoomIn();
                updateZoomLevel(true);
            } else if (v.equals(zoomOutImage)) {
                tMapView.mapZoomOut();
                updateZoomLevel(false);
            } else if (v.equals(locationImage)){
                locationImage.setSelected(!locationImage.isSelected());
                setTrackingMode(locationImage.isSelected());
            }
        }
    };

    private void setTracking(boolean isTracking) {
        TMapGpsManager manager = new TMapGpsManager(this);
        if (isTracking) {
            manager.setOnLocationChangeListener(locationListener);
            manager.setProvider(TMapGpsManager.PROVIDER_GPS);
            manager.openGps();
            manager.setProvider(TMapGpsManager.PROVIDER_NETWORK);
            manager.openGps();

            tMapView.setTrackingMode(true);
            tMapView.setCompassMode(true);
        } else {
            tMapView.setTrackingMode(false);
            tMapView.setCompassMode(false);
            manager.setOnLocationChangeListener(null);
        }
    }

    private final TMapGpsManager.OnLocationChangedListener locationListener = new TMapGpsManager.OnLocationChangedListener() {
        @Override
        public void onLocationChange(Location location) {
            if (location != null) {
                tMapView.setLocationPoint(location.getLatitude(), location.getLongitude());
                nowpoint = new TMapPoint(location.getLatitude(), location.getLongitude());
                mylocation = location;
            }
        }
    };
    private void setTrackingMode(boolean isTracking) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isGranted = true;
            String[] permissionArr = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ArrayList<String> checkPer = new ArrayList<>();
            for (String per : permissionArr) {
                if (checkSelfPermission(per) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    checkPer.add(per);
                    isGranted = false;
                }
            }

            if (isGranted) {
                setTracking(isTracking);
            } else {
                requestPermissions(checkPer.toArray(new String[0]), 100);
            }

        }
    }
    private void centerPositon(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                nowpoint = new TMapPoint(location.getLatitude(), location.getLongitude());
                mylocation = location;
                // 출발지 마커를 추가합니다.
                tMapView.setCenterPoint(nowpoint.getLatitude(), nowpoint.getLongitude());
                tMapView.setZoomLevel(15);
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}
