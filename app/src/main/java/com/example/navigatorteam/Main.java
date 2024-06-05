package com.example.navigatorteam;

import static com.example.navigatorteam.Support.CrimeType.getIconResourceByCrimeType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import org.w3c.dom.Element;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.navigatorteam.Class.CrimeZone;
import com.example.navigatorteam.Class.Spot;
import com.example.navigatorteam.Manager.SpotController;
import com.example.navigatorteam.Support.CrimeType;
import com.example.navigatorteam.databinding.ActivityMainBinding;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapInfo;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;
import com.skt.tmap.overlay.TMapCircle;
import com.skt.tmap.overlay.TMapMarkerItem;
import com.skt.tmap.overlay.TMapPolyLine;
import com.skt.tmap.poi.TMapPOIItem;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity implements GPSManager.LocationUpdateListener {
    private GPSManager gpsManager;
    private ImageView zoomInImage;
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
    private AutoCompleteListAdapter autoCompleteListAdapter;
    private TMapPoint nowpoint;

    @Override
    public void onLocationUpdated(double latitude, double longitude) {
        // 현재 위치가 업데이트되면 호출됩니다.
        nowpoint = new TMapPoint(latitude, longitude);

        addMarker2(nowpoint, "현재 위치", "현 위치입니다.");


        // 여기서 위치 정보를 사용하여 필요한 작업을 수행합니다.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gpsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void addMarker2(TMapPoint point, String title, String subtitle) {
        TMapMarkerItem marker = new TMapMarkerItem();
        marker.setId("현재위치");
        marker.setTMapPoint(point);
        marker.setCanShowCallout(true);
        marker.setCalloutTitle(title);
        marker.setCalloutSubTitle(subtitle);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.poi_dot);
        marker.setIcon(icon);
        tMapView.addTMapMarkerItem(marker);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TMapView 초기화
        tMapView = new TMapView(this);
        binding.tmapViewContainer.addView(tMapView);

        // TMapView 설정
        tMapView.setSKTMapApiKey("pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0");
        gpsManager = new GPSManager(this, tMapView, this);
        // 맵 로딩 완료 리스너 설정
        tMapView.setOnMapReadyListener(new TMapView.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // 맵 로딩 완료 후 처리할 작업
                Log.d("MainActivity", "TMapView is ready");
                gpsManager.getLocation();
                gpsManager.startLocationUpdates();
                initAutoComplete();
                showAll();
            }
        });
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomInImage.setOnClickListener(onClickListener);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        zoomOutImage.setOnClickListener(onClickListener);

        // 어댑터 초기화
        autoCompleteListAdapter = new AutoCompleteListAdapter(this);

        // 뷰 초기화
        zoomInImage = findViewById(R.id.zoomInImage);
        zoomOutImage = findViewById(R.id.zoomOutImage);
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteEdit = findViewById(R.id.autoCompleteEdit);
        autoCompleteListView = findViewById(R.id.autoCompleteListView);
        routeLayout = findViewById(R.id.routeLayout);
        routeDistanceTextView = findViewById(R.id.routeDistanceText);
        routeTimeTextView = findViewById(R.id.routeTimeText);
        routedescriptionTextView = findViewById(R.id.routedescriptionTextView);

        // 어댑터 설정
        autoCompleteListView.setAdapter(autoCompleteListAdapter);

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
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(zoomInImage)) {
                tMapView.mapZoomIn();
            } else if (v.equals(zoomOutImage)) {
                tMapView.mapZoomOut();
            }
        }
    };


    private void initAutoComplete() {
        autoCompleteLayout = findViewById(R.id.autoCompleteLayout);
        autoCompleteLayout.setVisibility(View.VISIBLE);
        autoCompleteEdit = findViewById(R.id.autoCompleteEdit);
        autoCompleteListView = findViewById(R.id.autoCompleteListView);
        autoCompleteListAdapter = new AutoCompleteListAdapter(this);
        autoCompleteListView.setAdapter(autoCompleteListAdapter);
        autoCompleteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String keyword = (String) autoCompleteListAdapter.getItem(position);

                findAllPoi(keyword);
                autoCompleteLayout.setVisibility(View.GONE);
            }
        });

        autoCompleteEdit.addTextChangedListener(new TextWatcher() {
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
                                autoCompleteListAdapter.setItemList(itemList);
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
    }


    public void findAllPoi(String strData) {
        TMapData tmapdata = new TMapData();
        tmapdata.findAllPOI(strData, new TMapData.OnFindAllPOIListener() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItemList) {
                showPOIResultDialog(poiItemList);
            }
        });
    }

    private void showPOIResultDialog(final ArrayList<TMapPOIItem> poiItem) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (poiItem != null) {
                    CharSequence[] item = new CharSequence[poiItem.size()];
                    for (int i = 0; i < poiItem.size(); i++) {
                        item[i] = poiItem.get(i).name;
                    }
                    new AlertDialog.Builder(Main.this)
                            .setTitle("POI 검색 결과")
                            .setIcon(R.drawable.icon)
                            .setItems(item, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    initAll();
                                    TMapPOIItem poi = poiItem.get(i);
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
                            }).create().show();
                }

            }
        });

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

}
