package com.example.navigatorteam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.Manifest;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigatorteam.Class.CrimeZone;
import com.example.navigatorteam.Class.Spot;
import com.example.navigatorteam.Manager.SafeReturnService;
import com.example.navigatorteam.Manager.SpotController;
import com.example.navigatorteam.Support.CrimeType;
import com.example.navigatorteam.Support.LocationInfo;
import com.example.navigatorteam.databinding.ActivityMainBinding;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapGpsManager;
import com.skt.tmap.TMapInfo;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;
import com.skt.tmap.address.TMapAddressInfo;
import com.skt.tmap.overlay.TMapCircle;
import com.skt.tmap.overlay.TMapMarkerItem;
import com.skt.tmap.overlay.TMapPolyLine;
import com.skt.tmap.poi.TMapPOIItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WalkingRouteActivity<ReverseGeocoding> extends AppCompatActivity implements LocationListener {

    private TMapPolyLine currentPolyline;
    private List<TMapPoint> routePoints;
    private int currentRouteIndex;

    private String nowAddress;
    private SafeReturnService safeReturnService;
    private ImageView zoomInImage;
    private LinearLayout routeLayout;
    private TextView routeDistanceTextView;
    private TextView routeTimeTextView;
    private TextView routedescriptionTextView;
    private ImageView zoomOutImage;
    private TextView zoomLevelTextView;
    private LinearLayout routeInfo;
    private int timetotal;
    private ActivityMainBinding binding;
    private LinearLayout autoCompleteLayout;
    private EditText autoCompleteEditStart;
    private EditText autoCompleteEditEnd;
    private TMapView tMapView;
    private ListView autoCompleteListViewStart;
    private ListView autoCompleteListViewEnd;
    private AutoCompleteListAdapter autoCompleteListAdapterStart;
    private AutoCompleteListAdapter autoCompleteListAdapterEnd;
    private TMapPoint startPoint;
    private TMapPoint endPoint;
    private TMapPoint nowposition;
    private Document pathDocument;
    private TMapPoint positon;
    private String endAddress;
    private String clickMarker;
    private String startname;

    private LinearLayout routeDescriptionLayout;
    private String endname;

    private Location previousLocation;
    private double totalDistance = 0.0;
    private LocationManager locationManager;
    private boolean firstLocationUpdate = true;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1.0f;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean flag = false;
    @Override
    public void onLocationChanged(Location location) {
        // 위치가 변경될 때 호출됩니다.
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        // 위치 정보를 사용할 수 있습니다. 여기서는 토스트 메시지로 출력합니다.
        if (LocationInfo.isDanger(longitude, latitude) && !flag) {
            showAlert(WalkingRouteActivity.this, "알림", "위험 지역입니다 주의하세요!");
            flag = true;
        } else {
            flag = false;
        }
        TMapPoint loca = new TMapPoint(latitude, longitude);
        if (endPoint != null && loca.getLatitude() == endPoint.getLatitude() && loca.getLongitude() == endPoint.getLongitude()) {
            Intent intent = new Intent(WalkingRouteActivity.this, SafeReturnService.class);
            stopService(intent);
            safeReturnService.onArrival();
        }

        // 현재 위치와 경로 지점 사이의 거리 계산
        if (routePoints != null && !routePoints.isEmpty()) {
            TMapPoint currentLocation = new TMapPoint(latitude, longitude);
            TMapPoint targetPoint = routePoints.get(currentRouteIndex);

            double distance = calculateDistance(currentLocation, targetPoint);
            if (distance < 50) { // 현재 위치와 다음 경로 지점 사이의 거리가 50m 이하인 경우
                currentRouteIndex++;
                if (currentRouteIndex < routePoints.size()) {
                    // 다음 경로 안내 표시
                    showRouteDescription(currentRouteIndex);
                } else {
                    // 경로 완료
                    currentRouteIndex = 0;
                    routePoints.clear();
                    routeDescriptionLayout.setVisibility(View.GONE);
                }
            }
        }
    }
    private double calculateDistance(TMapPoint start, TMapPoint end) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(end.getLatitude() - start.getLatitude());
        double dLng = Math.toRadians(end.getLongitude() - start.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(start.getLatitude())) * Math.cos(Math.toRadians(end.getLatitude())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
    private void showRouteDescription(int index) {
        if (index < routePoints.size()) {
            TMapPoint point = routePoints.get(index);
            // 경로 안내 텍스트 업데이트 (필요에 따라 추가 설명 작성)
            routedescriptionTextView.setText("다음 경로: " + point.toString());
        }
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
                // 위치 정보를 사용할 수 있습니다. 여기서는 토스트 메시지로 출력합니다.

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
    private int zoomIndex = -1;
    private ImageView locationImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_walking_route);

        // 위치 권한 확인 및 요청
        checkLocationPermission();


        tMapView = new TMapView(this);
        FrameLayout mapViewContainer = findViewById(R.id.tmapViewContainer); // 맵을 추가할 컨테이너 레이아웃 찾기
        mapViewContainer.addView(tMapView);
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

        // SpotController 초기화
        SpotController.getInstance().init(this);

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

        // 맵 로딩 완료 리스너 설정
        tMapView.setOnMapReadyListener(new TMapView.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // 맵 로딩 완료 후 처리할 작업
                Log.d("MainActivity", "TMapView is ready");
                initAutoComplete();
                setNowposition();
                addMarkersAndCircles(CrimeZoneController.GetInstance().GetCrimeZones());
                totalDistance = 0.0;
                int zoom = tMapView.getZoomLevel();
                zoomLevelTextView.setText("Lv." + zoom);
            }
        });
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomInImage.setOnClickListener(onClickListener);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        zoomOutImage.setOnClickListener(onClickListener);
        locationImage = findViewById(R.id.locationImage);
        locationImage.setOnClickListener(onClickListener);


        // 뷰 초기화
        zoomLevelTextView = findViewById(R.id.zoomLevelText);
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteEditStart = findViewById(R.id.autoCompleteEditStart);
        autoCompleteEditEnd = findViewById(R.id.autoCompleteEditEnd);
        autoCompleteListViewStart = findViewById(R.id.autoCompleteListViewStart);
        autoCompleteListViewEnd = findViewById(R.id.autoCompleteListViewEnd);
        routeLayout = findViewById(R.id.routeLayout); // 전체 레이아웃
        routeInfo = findViewById(R.id.routeInfoLayout); // 총 시간 및 총 거리
        routeDescriptionLayout = findViewById(R.id.routeDescriptionLayout); // 경로 안내
        routeDistanceTextView = findViewById(R.id.routeDistanceText);
        routeTimeTextView = findViewById(R.id.routeTimeText);
        routedescriptionTextView = findViewById(R.id.routedescriptionTextView);

        // 어댑터 초기화
        autoCompleteListAdapterStart = new AutoCompleteListAdapter(this);
        autoCompleteListAdapterEnd = new AutoCompleteListAdapter(this);

        // 어댑터 설정
        autoCompleteListViewStart.setAdapter(autoCompleteListAdapterStart);
        autoCompleteListViewEnd.setAdapter(autoCompleteListAdapterEnd);
        initAutoComplete();

        autoCompleteListViewStart = findViewById(R.id.autoCompleteListViewStart);
        autoCompleteListViewEnd = findViewById(R.id.autoCompleteListViewEnd);
        Button ReButton = findViewById(R.id.Rebutton);
        Button findRouteButton = findViewById(R.id.findPathButton); //길찾기 버튼
        Button WalikngRoutebutton = findViewById(R.id.WalikngRoutebutton);
        tMapView.setOnEnableScrollWithZoomLevelListener(new TMapView.OnEnableScrollWithZoomLevelCallback() {
            @Override
            public void onEnableScrollWidthZoomLevel(float v, TMapPoint tMapPoint) {
                int zoom = (int) v;
                zoomLevelTextView.setText("Lv." + zoom);
                zoomIndex = zoom - 6;
            }
        });
        WalikngRoutebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // UI 업데이트 코드 작성
                        hideKeyboard();
                        tMapView.removeAllTMapMarkerItem();
                        showAll();
                        autoCompleteLayout.setVisibility(View.GONE);
                        ReButton.setVisibility(View.VISIBLE);
                        findRouteButton.setVisibility(View.VISIBLE);
                    }
                });
                findPathAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, new TMapData.OnFindPathDataAllTypeListener() {
                    @Override
                    public void onFindPathDataAllType(Document doc) {
                        // 경로 안내가 완료되었음을 표시하고 길찾기 버튼을 활성화
                        pathDocument = doc;
                    }
                });
            }
        });
        ReButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCompleteEditStart.setText("");
                autoCompleteEditEnd.setText("");
                autoCompleteLayout.setVisibility(View.VISIBLE);
                routeInfo.setVisibility(View.GONE);
                routeDescriptionLayout.setVisibility(View.GONE);
                routeLayout.setVisibility(View.GONE);
            }
        });
        findRouteButton.setOnClickListener(new View.OnClickListener() { // 길찾기 버튼 눌렀을 때
            @Override
            public void onClick(View v) {
                showSafeReturnPromptDialog();
            }
        });
        Button homebutton = findViewById(R.id.homebutton);
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalkingRouteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Button search_placeButton = findViewById(R.id.search_place);
        search_placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalkingRouteActivity.this, Main.class);
                startActivity(intent);
            }
        });
        Button currentLocationButton = findViewById(R.id.currentLocationButton); // 현재 위치 설정 버튼
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 위치를 가져와 출발지로 설정하는 함수 호출
                setCurrentLocationAsStartPoint();

            }
        });
        routePoints = new ArrayList<>();
        currentRouteIndex = 0;

    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

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
    private void setTrackingMode(boolean isTracking) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isGranted = true;
            String[] permissionArr = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
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

    private void initAutoComplete() {
        autoCompleteLayout.setVisibility(View.VISIBLE);
        autoCompleteEditStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                TMapData tMapData = new TMapData();
                tMapData.autoComplete(keyword, new TMapData.OnAutoCompleteListener() {
                    @Override
                    public void onAutoComplete(ArrayList<String> itemList) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                autoCompleteListViewStart.setVisibility(View.VISIBLE);
                                autoCompleteListAdapterStart.setItemList(itemList);
                            }
                        });
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        autoCompleteEditEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                TMapData tMapData = new TMapData();
                tMapData.autoComplete(keyword, new TMapData.OnAutoCompleteListener() {
                    @Override
                    public void onAutoComplete(ArrayList<String> itemList) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                autoCompleteListAdapterEnd.setItemList(itemList);
                                autoCompleteListViewEnd.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        autoCompleteListViewStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String keyword = (String) autoCompleteListAdapterStart.getItem(position);
                findAllPoi(keyword, true);
                autoCompleteListViewStart.setVisibility(View.GONE);

            }
        });

        autoCompleteListViewEnd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String keyword = (String) autoCompleteListAdapterEnd.getItem(position);
                findAllPoi(keyword, false);
                autoCompleteListViewEnd.setVisibility(View.GONE);

            }
        });
    }

    public void findAllPoi(String strData, boolean isStartPoint) {
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(strData, new TMapData.OnFindAllPOIListener() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItemList) {
                showPOIResultDialog(poiItemList, isStartPoint);
            }
        });
    }


    private void showPOIResultDialog(final ArrayList<TMapPOIItem> poiItem, boolean isStartPoint) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (poiItem != null) {
                    CharSequence[] item = new CharSequence[poiItem.size()];
                    for (int i = 0; i < poiItem.size(); i++) {
                        item[i] = poiItem.get(i).name;
                    }
                    AlertDialog dialog = new AlertDialog.Builder(WalkingRouteActivity.this)
                            .setTitle("검색 결과입니다.")
                            .setIcon(R.drawable.black_sc) // 포이 검색 결과 아이콘 수정해야 함
                            .setItems(item, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    TMapPOIItem poi = poiItem.get(i);
                                    if (isStartPoint) {
                                        startPoint = poi.getPOIPoint();
                                        autoCompleteEditStart.setText(poi.getPOIName());
                                        startname = poi.getPOIName();
                                        autoCompleteListViewStart.setVisibility(View.GONE);
                                    } else {
                                        endPoint = poi.getPOIPoint();
                                        EndreverseGeocoading(endPoint);
                                        autoCompleteEditEnd.setText(poi.getPOIName());
                                        endname = poi.getPOIName();
                                        autoCompleteListViewEnd.setVisibility(View.GONE);
                                    }
                                    addMarker(poi);
                                }
                            }).create();
                    dialog.show();
                }
            }
        });
    }


    private void addMarker(TMapPOIItem poi) {
        TMapMarkerItem marker = new TMapMarkerItem();
        marker.setId(poi.id);
        marker.setTMapPoint(poi.getPOIPoint());
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);
        marker.setIcon(icon);
        marker.setCalloutTitle(poi.getPOIName());
        marker.setCalloutSubTitle("주소:" + poi.getPOIAddress());
        marker.setCanShowCallout(true);
        marker.setAnimation(true);
        tMapView.addTMapMarkerItem(marker);
        tMapView.setCenterPoint(poi.getPOIPoint().getLatitude(), poi.getPOIPoint().getLongitude());
    }
    private void findPathAllType(final TMapData.TMapPathType type, final TMapData.OnFindPathDataAllTypeListener listener) {
        TMapData data = new TMapData();
        data.findPathDataAllType(type, startPoint, endPoint, new TMapData.OnFindPathDataAllTypeListener() {
            @Override
            public void onFindPathDataAllType(Document doc) {
                tMapView.removeTMapPath(); // 기존 경로 제거

                TMapPolyLine polyline = new TMapPolyLine();
                polyline.setID(type.name());
                polyline.setLineWidth(10);
                polyline.setLineColor(Color.RED);
                polyline.setLineAlpha(255);

                if (doc != null) {
                    NodeList list = doc.getElementsByTagName("Document");
                    Element item2 = (Element) list.item(0);
                    String totalDistance = getContentFromNode(item2, "tmap:totalDistance");
                    String totalTime = getContentFromNode(item2, "tmap:totalTime");

                    NodeList list2 = doc.getElementsByTagName("LineString");

                    for (int i = 0; i < list2.getLength(); i++) {
                        Element item = (Element) list2.item(i);
                        String str = getContentFromNode(item, "coordinates");
                        if (str == null) {
                            continue;
                        }

                        String[] str2 = str.split(" ");
                        for (int k = 0; k < str2.length; k++) {
                            try {
                                String[] str3 = str2[k].split(",");
                                TMapPoint point = new TMapPoint(Double.parseDouble(str3[1]), Double.parseDouble(str3[0]));
                                polyline.addLinePoint(point);
                                routePoints.add(point); // 경로 지점 추가
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    tMapView.setTMapPath(polyline);
                    currentPolyline = polyline;

                    TMapInfo info = tMapView.getDisplayTMapInfo(polyline.getLinePointList());

                    tMapView.setZoomLevel(16);
                    tMapView.setCenterPoint(info.getPoint().getLatitude(), info.getPoint().getLongitude());

                    setPathText(totalDistance, totalTime);

                    // 첫 번째 경로 안내 표시
                    currentRouteIndex = 0;
                    showRouteDescription(currentRouteIndex);

                    // 새로 추가된 부분: 외부에서 전달된 리스너를 호출하여 처리 결과를 전달
                    listener.onFindPathDataAllType(doc);
                }
            }
        });
    }
    private void displayPathDetails(Document doc) {
        if (doc != null) {
            NodeList placemarkList = doc.getElementsByTagName("Placemark");
            List<String> descriptionList = new ArrayList<>();

            for (int i = 0; i < placemarkList.getLength(); i++) {
                Element placemark = (Element) placemarkList.item(i);
                String description = getContentFromNode(placemark, "description");
                descriptionList.add(description + "\n");
            }
            List<String> evenIndexDescriptions = new ArrayList<>();
            for (int k = 0; k < descriptionList.size(); k++) {
                if (k % 2 == 0) { // 짝수 인덱스 확인
                    evenIndexDescriptions.add(descriptionList.get(k));
                }
            }
            routedescriptionTextView.setText(evenIndexDescriptions.toString());
        }
    }
    private void setPathText(String distance, String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                routeLayout.setVisibility(View.VISIBLE);
                routeInfo.setVisibility(View.VISIBLE);
                double km = Double.parseDouble(distance) / 1000;
                double roundedKm = Math.round(km * 10.0) / 10.0;
                routeDistanceTextView.setText("총 거리 : " + roundedKm + " km  ");

                int totalSec = Integer.parseInt(time);
                int day = totalSec / (60 * 60 * 24);
                int hour = (totalSec - day * 60 * 60 * 24) / (60 * 60);
                int minute = (totalSec - day * 60 * 60 * 24 - hour * 3600) / 60;
                String t;
                if (hour > 0) {
                    t = hour + "시간 " + minute + "분";
                } else {
                    t = minute + "분 ";
                }
                timetotal = minute;
                routeTimeTextView.setText("예상시간 : 약 " + t);
            }
        });
    }
    private String getContentFromNode(Element item, String tagName) {
        NodeList list = item.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            if (list.item(0).getFirstChild() != null) {
                return list.item(0).getFirstChild().getNodeValue();
            }
        }
        return null;
    }


    private void addMarker2(TMapPoint point, String Id, String title, String Address) {
        tMapView.removeTMapMarkerItem("현재위치");
        TMapMarkerItem marker = new TMapMarkerItem();
        marker.setId(Id);
        marker.setTMapPoint(point);
        marker.setCanShowCallout(true);
        marker.setCalloutTitle(title);
        marker.setCalloutSubTitle(Address);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);
        marker.setIcon(icon);
        tMapView.addTMapMarkerItem(marker);
        String id = marker.getId();
        Log.d("TAG", "addMarker2 ID : " + id);
    }
    private void setCurrentLocationAsStartPoint() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location == null) {
                // 네트워크를 통해 현재 위치를 가져옵니다.
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                startPoint = new TMapPoint(location.getLatitude(), location.getLongitude());
                NowreverseGeocoading(startPoint);
                autoCompleteEditStart.setText(nowAddress);
                // 출발지 마커를 추가합니다.
               setTracking(true);
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setNowposition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location = (gpsLocation != null) ? gpsLocation : networkLocation;

            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                nowposition = new TMapPoint(location.getLatitude(), location.getLongitude());
                // 출발지 마커를 추가합니다.
                tMapView.setZoomLevel(15);
                tMapView.setCenterPoint(nowposition.getLatitude(), nowposition.getLongitude());
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void centerPositon(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            // GPS를 통해 현재 위치를 가져옵니다.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 현재 위치를 출발지로 설정합니다.
                nowposition = new TMapPoint(location.getLatitude(), location.getLongitude());
                // 출발지 마커를 추가합니다.
                tMapView.setCenterPoint(nowposition.getLatitude(), nowposition.getLongitude());
                tMapView.setZoomLevel(19);
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    private void NowreverseGeocoading(TMapPoint tMapPoint) {
        TMapData tMapData = new TMapData();
        tMapData.reverseGeocoding(tMapPoint.getLatitude(), tMapPoint.getLongitude(), "A10", new TMapData.OnReverseGeocodingListener() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo info) {
                if (info != null) {
                    // 주소 정보를 가공하여 변수에 저장
                    String address = "";
                    if (info.strLegalDong != null && !info.strLegalDong.equals("")) {
                        address += info.strCity_do + " " + info.strGu_gun + " " + info.strLegalDong;
                        if (info.strRi != null && !info.strRi.equals("")) {
                            address += (" " + info.strRi);
                        }
                        address += (" " + info.strBunji);
                    } else if (info.strRoadName != null && !info.strRoadName.equals("")) {
                        address += info.strCity_do + " " + info.strGu_gun + " " + info.strRoadName + " " + info.strBuildingIndex;
                    }
                    // 변수에 저장
                    nowAddress = address;
                    Log.d("ddd", "현재 주소: " + address);
                }
            }
        });
    }
    private void EndreverseGeocoading(TMapPoint tMapPoint) {
        TMapData tMapData = new TMapData();
        tMapData.reverseGeocoding(tMapPoint.getLatitude(), tMapPoint.getLongitude(), "A10", new TMapData.OnReverseGeocodingListener() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo info) {
                if (info != null) {
                    // 주소 정보를 가공하여 변수에 저장
                    String address = "";
                    if (info.strLegalDong != null && !info.strLegalDong.equals("")) {
                        address += info.strCity_do + " " + info.strGu_gun + " " + info.strLegalDong;
                        if (info.strRi != null && !info.strRi.equals("")) {
                            address += (" " + info.strRi);
                        }
                        address += (" " + info.strBunji);
                    } else if (info.strRoadName != null && !info.strRoadName.equals("")) {
                        address += info.strCity_do + " " + info.strGu_gun + " " + info.strRoadName + " " + info.strBuildingIndex;
                    }
                    // 변수에 저장
                    endAddress = address;
                    Log.d("ddd", "도착 주소: " + address);
                }
            }
        });
    }
    private void ClickReverse(TMapPoint tMapPoint) {
        TMapData tMapData = new TMapData();
        tMapData.reverseGeocoding(tMapPoint.getLatitude(), tMapPoint.getLongitude(), "A10", new TMapData.OnReverseGeocodingListener() {
            @Override
            public void onReverseGeocoding(TMapAddressInfo info) {
                if (info != null) {
                    // 주소 정보를 가공하여 변수에 저장
                    String address = "";
                    if (info.strLegalDong != null && !info.strLegalDong.equals("")) {
                        address += info.strCity_do + " " + info.strGu_gun + " " + info.strLegalDong;
                        if (info.strRi != null && !info.strRi.equals("")) {
                            address += (" " + info.strRi);
                        }
                        address += (" " + info.strBunji);
                    } else if (info.strRoadName != null && !info.strRoadName.equals("")) {
                        address += info.strCity_do + " " + info.strGu_gun + " " + info.strRoadName + " " + info.strBuildingIndex;
                    }
                    // 변수에 저장
                    clickMarker = address;
                    Log.d("ddd", "도착 주소: " + address);
                }
            }
        });
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
            int opacity = (int) ((crimeLevel / 5f) * 255); // 필요에 따라 이 계산을 조정합니다.

            // 마커 객체 생성
            TMapMarkerItem markerItem = new TMapMarkerItem();
            markerItem.setId("tmp_" + i);
            markerItem.setTMapPoint(lonlat);
            markerItem.setName(title);
            markerItem.setVisible(markerItem.getVisible());
            ;
            Bitmap icon = BitmapFactory.decodeResource(getResources(), CrimeType.getIconResourceByCrimeType(crimeZone.crimeType, WalkingRouteActivity.this));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(icon, 64, 64, false);
            markerItem.setIcon(scaledBitmap);
            tMapView.addTMapMarkerItem(markerItem);

            // 반경을 원으로 표시
            TMapCircle circle = new TMapCircle();
            circle.setId("tmp_circle_" + i);
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



    private void setTracking(boolean isTracking) {
        TMapGpsManager manager = new TMapGpsManager(this);
        if (isTracking) {
            manager.setOnLocationChangeListener(locationListener);
            manager.setProvider(TMapGpsManager.PROVIDER_GPS);
            manager.openGps();
            manager.setProvider(TMapGpsManager.PROVIDER_NETWORK);
            manager.openGps();
            tMapView.setTrackingMode(true);
        } else {
            tMapView.setTrackingMode(false);
            manager.setOnLocationChangeListener(null);
        }
    }
    private final TMapGpsManager.OnLocationChangedListener locationListener = new TMapGpsManager.OnLocationChangedListener() {
        @Override
        public void onLocationChange(Location location) {
            if (location != null) {
                tMapView.setLocationPoint(location.getLatitude(), location.getLongitude());
                // 위치 정보를 사용할 수 있습니다. 여기서는 토스트 메시지로 출력합니다.
            }
        }
    };
    public void makeEmergencyCall() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:112"));
        startActivity(callIntent);
    }

    private AlertDialog alertDialog;

    public void showAlert(Context context, String title, String message) {
        // 기존 다이얼로그가 존재하면 새로운 다이얼로그를 띄우지 않음
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        Button positiveButton = dialogView.findViewById(R.id.positive_button);
        Button negativeButton = dialogView.findViewById(R.id.negative_button);

        titleView.setText(title);
        messageView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        alertDialog = builder.create(); // 다이얼로그 객체를 멤버 변수에 저장

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 확인 버튼을 클릭했을 때의 동작
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEmergencyCall();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }


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
        SpotController.getInstance().init(this);
        addSpots(SpotController.getInstance().getSpots());
    }

    private void showFacilities() {
        hideAll();
        addSpots(SpotController.getInstance().getSpots());
    }

    private void showCrimes() {
        hideAll();
        addMarkersAndCircles(CrimeZoneController.GetInstance().GetCrimeZones());
    }

    private void hideAll() {
        tMapView.removeAllTMapMarkerItem();
        tMapView.removeAllTMapCircle();
    }
    private void showSafeReturnPromptDialog() {
        // 다이얼로그 레이아웃을 inflate
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_safe_return_prompt, null);

        final Button noButton = dialogView.findViewById(R.id.noButton);
        final Button yesButton = dialogView.findViewById(R.id.yesButton);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pathDocument != null) {
                    tMapView.removeAllTMapMarkerItem();
                    showAll();
                    displayPathDetails(pathDocument);
                    centerPositon();
                    addMarker2(endPoint, "도착지", "(도착)" + endname, endAddress);
                    routeInfo.setVisibility(View.GONE);
                    routeDescriptionLayout.setVisibility(View.VISIBLE);

                }
                dialog.dismiss();
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openSafeReturnDialog();
            }
        });

        dialog.show();
    }
    private void openSafeReturnDialog() {
        // 다이얼로그 레이아웃을 inflate
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_safe_return, null);

        final EditText phoneNumberInput = dialogView.findViewById(R.id.phone_number_input);
        final Button startServiceButton = dialogView.findViewById(R.id.start_service_button);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberInput.getText().toString();

                if (!phoneNumber.isEmpty()) {
                    Log.d("SafeReturnDialog", "w " + phoneNumber);

                    Intent intent = new Intent(WalkingRouteActivity.this, SafeReturnService.class);
                    intent.putExtra("estimatedTimeInMinutes", timetotal);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startService(intent);
                    Log.d("TAG", "안심귀가 눌름: " + timetotal + phoneNumber);
                    dialog.dismiss();
                    if (pathDocument != null) {
                        tMapView.removeAllTMapMarkerItem();
                        showAll();
                        displayPathDetails(pathDocument);
                        centerPositon();
                        addMarker2(endPoint, "도착지", "(도착)" + endname, endAddress);
                        routeInfo.setVisibility(View.GONE);
                        routeDescriptionLayout.setVisibility(View.VISIBLE);
                    }

                } else {
                    Log.d("TAG", "전화번호 안눌름: ");
                    Toast.makeText(WalkingRouteActivity.this, "전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
        Log.d("SafeReturnDialog", "Dialog shown.");
    }
}