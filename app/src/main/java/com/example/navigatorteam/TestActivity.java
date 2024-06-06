package com.example.navigatorteam;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.example.navigatorteam.databinding.TestBinding;
import com.skt.tmap.TMapGpsManager;
import com.skt.tmap.TMapPoint;
import com.skt.tmap.TMapView;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {
    private TMapView tMapView;
    private TestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding 초기화
        binding = TestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TMapView 초기화
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("pRNUlsEpce4d3mB0MUabnMDhHbLmdtlPrUYZI3i0");

        // TMapView를 레이아웃에 추가
        binding.tmapViewContainer.addView(tMapView);

        tMapView.setOnMapReadyListener(new TMapView.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // 맵 로딩 완료 후 처리할 작업
                Log.d("TestActivity", "TMapView is ready");
                setTracking(true);
            }
        });
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
            }
        }
    };
}
