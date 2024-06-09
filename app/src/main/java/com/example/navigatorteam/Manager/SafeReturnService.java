package com.example.navigatorteam.Manager;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navigatorteam.Main;
import com.example.navigatorteam.MainActivity;
import com.example.navigatorteam.WalkingRouteActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SafeReturnService extends Service {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private Context context;
    private Handler handler = new Handler();
    private Runnable runnable;
    private long startTime;
    private String phoneNumber;
    private String startAdress;
    private String endAdress;
    private int estimatedTimeInMinutes;
    private String name;
    private String formattedCurrentTime;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Log.d("TAG", "리턴서비스: ");
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
    public void startSafeReturn(int estimatedTimeInSeconds, String phoneNumber,String name, String StartAdress, String EndAdderess) {
        int minutes = estimatedTimeInSeconds / 60;
        int seconds = estimatedTimeInSeconds % 60;
        this.name = name;
        this.startAdress = StartAdress;
        this.endAdress = EndAdderess;
        this.estimatedTimeInMinutes = estimatedTimeInSeconds;
        this.phoneNumber = phoneNumber;
        this.startTime = System.currentTimeMillis();

        long currentTimeMillis = System.currentTimeMillis();
        Date currentTime = new Date(currentTimeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
        formattedCurrentTime = sdf.format(currentTime);

        Log.d("아", "startSafeReturn: " + minutes + "분 " + seconds + "초");

        sendSms(phoneNumber,
                "현재 '" + name + "'님이 안전 귀가 기능을 이용중입니다.\n" +
                        "출발지 : "+ StartAdress +
                        "\n도착지 : " + EndAdderess + "\n" +
                        "출발 시각 : " + formattedCurrentTime + "\n" +
                        "예상 도착 시각 : " + calculateEstimatedArrivalTime(currentTimeMillis, estimatedTimeInSeconds) + "\n"
                        + "예상 소요 시간은 " + minutes + "분 " + seconds + "초 입니다.\n"
                        + "도착시간이 지나도 사용자가 목표 위치에 진입하지 못할경우, 등록된 사용자에게 문자가 표시되니 확인하 여 주시기 바랍니다.\n"
                        + "\n감사합니다."
        );
        Log.d("TAG", "문자 안내: " + "현재 '" + name + "'님이 안전 귀가 기능을 이용중입니다.\n" +
                "출발지 : "+ StartAdress +
                "\n도착지 : " + EndAdderess + "\n" +
                "출발 시각 : " + formattedCurrentTime + "\n" +
                "예상 도착 시각 : " + calculateEstimatedArrivalTime(currentTimeMillis, estimatedTimeInSeconds) + "\n"
                + "예상 소요 시간은 " + minutes + "분 " + seconds + "초 입니다.\n"
                + "도착시간이 지나도 사용자가 목표 위치에 진입하지 못할경우, 등록된 사용자에게 문자가 표시되니 확인하여 주시기 바랍니다.\n"
                + "\n감사합니다.");
        checkRouteProgress();
    }

    private String calculateEstimatedArrivalTime(long currentTimeMillis, int estimatedTimeInSeconds) {
        long estimatedArrivalTimeMillis = currentTimeMillis + (estimatedTimeInSeconds * 1000); // Add estimated time in milliseconds
        Date estimatedArrivalTime = new Date(estimatedArrivalTimeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
        return sdf.format(estimatedArrivalTime);
    }
    private void checkRouteProgress() {
        final SimpleDateFormat sdf = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
        runnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTimeMillis = System.currentTimeMillis() - startTime; // 경과 시간 (밀리초 단위)
                long elapsedMinutes = elapsedTimeMillis / (1000 * 60); // 분 단위로 변환
                long elapsedSeconds = (elapsedTimeMillis / 1000) % 60; // 초 단위로 변환

                long estimatedArrivalTimeMillis = startTime + (estimatedTimeInMinutes * 60 * 1000);
                Date estimatedArrivalTime = new Date(estimatedArrivalTimeMillis);

                if (elapsedMinutes > estimatedTimeInMinutes * 1.3) {
                    sendSms(phoneNumber,  "'"+name + "'님이 안심 귀가 기능을 이용하였으나, \n " +
                            "예상 도착시간보다 30% 이상 지연되고 있습니다. \n" +
                            "출발시간 : " + formattedCurrentTime +"\n" +
                            "도착 지연 시간 : " + sdf.format(estimatedArrivalTime) + "\n" +
                            "현재까지" + elapsedMinutes + "분 " + elapsedSeconds + "초 경과.");
                }
                if (elapsedMinutes > estimatedTimeInMinutes * 1.5) {
                    sendSms(phoneNumber,  "'"+name + "'님이 안심 귀가 기능을 이용하였으나, \n " +
                            "예상 도착시간보다 50% 이상 지연되고 있습니다. \n" +
                            "출발시간 : " + formattedCurrentTime +"\n" +
                            "도착 지연 시간 : " + sdf.format(estimatedArrivalTime) + "\n" +
                            "현재까지" + elapsedMinutes + "분 " + elapsedSeconds + "초 경과.");
                }
                if (elapsedMinutes > estimatedTimeInMinutes * 2) {
                    sendSms(phoneNumber,  "'"+name + "'님이 안심 귀가 기능을 이용하였으나, \n " +
                            "예상 도착시간보다 100% 이상 지연되고 있습니다. \n" +
                            "출발시간 : " + formattedCurrentTime +"\n" +
                            "도착 지연 시간 : " + sdf.format(estimatedArrivalTime) + "\n" +
                            "현재까지" + elapsedMinutes + "분 " + elapsedSeconds + "초 경과.");
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
        Log.d("TAG", "스타트안됌 : " + intent);
        if (intent != null) {
            estimatedTimeInMinutes = intent.getIntExtra("estimatedTimeInMinutes", 0);
            startAdress = intent.getStringExtra("start");
            endAdress = intent.getStringExtra("end");
            phoneNumber = intent.getStringExtra("phoneNumber");
            name = intent.getStringExtra("name");
            // 여기서 권한을 확인합니다.
            // startSafeReturn 메서드는 권한 확인 후에 호출되어야 합니다.
            startSafeReturn(estimatedTimeInMinutes, phoneNumber, name ,startAdress, endAdress);
            Log.d("TAG", "스타트누름 : " + phoneNumber);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
