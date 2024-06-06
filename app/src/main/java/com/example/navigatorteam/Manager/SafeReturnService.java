package com.example.navigatorteam.Manager;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navigatorteam.Main;
import com.example.navigatorteam.MainActivity;
import com.example.navigatorteam.WalkingRouteActivity;

public class SafeReturnService extends Service {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private Context context;
    private Handler handler = new Handler();
    private Runnable runnable;
    private long startTime;
    private String phoneNumber;
    private int estimatedTimeInMinutes;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        checkSmsPermission();
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 액티비티에서 권한을 요청
            ActivityCompat.requestPermissions((WalkingRouteActivity) context, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
    }
/*
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "SMS 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "SMS 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/
    private void sendSms(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    /*
     int estimatedTimeInMinutes = 30; // 예시: 예상 도착 시간 30분
                String phoneNumber = "01027199447"; // 예시: 수신자 전화번호
//                Intent intent = new Intent(MainActivity.this, SafeReturnService.class);
//                intent.putExtra("estimatedTimeInMinutes", estimatedTimeInMinutes);
//                intent.putExtra("phoneNumber", phoneNumber);
//                startService(intent);
     */
    public void startSafeReturn(int estimatedTimeInMinutes, String phoneNumber) {
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
        this.phoneNumber = phoneNumber;
        this.startTime = System.currentTimeMillis();

        sendSms(phoneNumber, "안전 귀가를 시작합니다. 예상 소요 시간은 " + estimatedTimeInMinutes + "분 입니다.");
        checkRouteProgress();
    }

    private void checkRouteProgress() {
        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 60000; // 경과 시간 (분 단위)

                if (elapsedTime > estimatedTimeInMinutes * 1.3) {
                    sendSms(phoneNumber, "예상 시간보다 30% 이상 지연되고 있습니다. 현재까지 " + elapsedTime + "분 경과.");
                }
                if (elapsedTime > estimatedTimeInMinutes * 1.5) {
                    sendSms(phoneNumber, "예상 시간보다 50% 이상 지연되고 있습니다. 현재까지 " + elapsedTime + "분 경과.");
                }
                if (elapsedTime > estimatedTimeInMinutes * 2) {
                    sendSms(phoneNumber, "예상 시간보다 100% 이상 지연되고 있습니다. 현재까지 " + elapsedTime + "분 경과.");
                }

                handler.postDelayed(this, 60000); // 1분마다 확인
            }
        };

        handler.post(runnable);
    }

    //도착시 실행 !!!!!!
    //Intent intent = new Intent(MainActivity.this, SafeReturnService.class);
    //stopService(intent);
    public void onArrival() {
        sendSms(phoneNumber, "안전 귀가 완료!");
        handler.removeCallbacks(runnable);
        stopSelf(); // 서비스 종료
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            estimatedTimeInMinutes = intent.getIntExtra("estimatedTimeInMinutes", 0);
            phoneNumber = intent.getStringExtra("phoneNumber");
            startSafeReturn(estimatedTimeInMinutes, phoneNumber);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
