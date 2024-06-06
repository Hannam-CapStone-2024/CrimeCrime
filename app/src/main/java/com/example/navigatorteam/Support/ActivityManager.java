package com.example.navigatorteam.Support;

import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigatorteam.Class.Crime;
import com.example.navigatorteam.Class.TimeRange;
import com.example.navigatorteam.Class.WeekType;
import com.example.navigatorteam.MainActivity;
import com.example.navigatorteam.R;
import com.skt.tmap.TMapData;
import com.skt.tmap.TMapPoint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ActivityManager {

    private static ActivityManager instance;
    private TextView theftStatusTextView;
    private TextView robberyStatusTextView;
    private TextView assaultStatusTextView;
    private TextView locationTextView;
    private TextView dateTimeTextView;
    private TextView totalTextView;
    private TextView explainTextView;

    private LinearLayout mainLayout;
    private LinearLayout subLayout;
    private TableLayout subTableRow;
    private TableRow subTableLowTop;

    private TextView date_1;
    private ImageView time_1;
    private TextView cond_1;

    private TextView date_2;
    private ImageView time_2;
    private TextView cond_2;

    private TextView date_3;
    private ImageView time_3;
    private TextView cond_3;

    private TextView date_4;
    private ImageView time_4;
    private TextView cond_4;

    private TextView date_5;
    private ImageView time_5;
    private TextView cond_5;

    private TextView date_6;
    private ImageView time_6;
    private TextView cond_6;
    ImageView mainIcon;

    // 싱글턴 인스턴스를 반환하는 메서드
    public static ActivityManager getInstance() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    public void initialize(TextView theftStatus,
                           TextView robberyStatus,
                           TextView assaultStatus,
                           TextView locationTextView,
                           TextView dateTimeTextView,
                           TextView totalTextView,
                           TextView explainTextView,
                           LinearLayout mainLayout,
                           LinearLayout subLayout,
                           TableLayout subTableRow,
                           TableRow subTableLowTop,
                           ImageView mainIcon,
                           TextView date_1,
                           ImageView time_1,
                           TextView cond_1,
                           TextView date_2,
                           ImageView time_2,
                           TextView cond_2,
                           TextView date_3,
                           ImageView time_3,
                           TextView cond_3,
                           TextView date_4,
                           ImageView time_4,
                           TextView cond_4,
                             TextView date_5,
                           ImageView time_5,
                           TextView cond_5,
                             TextView date_6,
                           ImageView time_6,
                           TextView cond_6
    ) {
        this.theftStatusTextView = theftStatus;
        this.robberyStatusTextView = robberyStatus;
        this.assaultStatusTextView = assaultStatus;
        this.locationTextView = locationTextView;
        this.dateTimeTextView = dateTimeTextView;
        this.totalTextView = totalTextView;
        this.explainTextView = explainTextView;
        this.mainLayout = mainLayout;
        this.subLayout = subLayout;
        this.subTableRow= subTableRow;
        this.subTableLowTop= subTableLowTop;
        this.mainIcon = mainIcon;

        this.date_1 = date_1;
        this.time_1 = time_1;
        this.cond_1 = cond_1;

        this.date_2 = date_2;
        this.time_2 = time_2;
        this.cond_2 = cond_2;

        this.date_3 = date_3;
        this. time_3 = time_3;
        this. cond_3 = cond_3;

        this. date_4 = date_4;
        this. time_4 = time_4;
        this. cond_4 = cond_4;

        this. date_5 = date_5;
        this. time_5 = time_5;
        this. cond_5 = cond_5;

        this. date_6 = date_6;
        this. time_6 = time_6;
        this. cond_6 = cond_6;
    }

    public void setCrimeStatus(String location) throws IOException {
        TimeRange currentTimeRange = getCurrentTimeRange();
        WeekType currentWeekType = getCurrentWeekType();

        if (theftStatusTextView != null) {
            theftStatusTextView.setText(Crime.EachState(CrimeType.Violent, currentTimeRange, currentWeekType, location));
        }
        if (robberyStatusTextView != null) {
            robberyStatusTextView.setText(Crime.EachState(CrimeType.Theft, currentTimeRange, currentWeekType, location));
        }
        if (assaultStatusTextView != null) {
            assaultStatusTextView.setText(Crime.EachState(CrimeType.Violence, currentTimeRange, currentWeekType, location));
        }
    }

    public void setExplainText(String location) throws IOException {
        TimeRange currentTimeRange = getCurrentTimeRange();
        WeekType currentWeekType = getCurrentWeekType();
        //String totalState = "좋음";
        String totalState = Crime.TotalState(currentTimeRange, currentWeekType, location);
        int color = setStatusColor(totalState);
        explainTextView.setText(totalState);
        totalTextView.setText(Crime.crimteExplain(totalState));

        mainLayout.setBackgroundColor(color);
        subLayout.setBackgroundColor(setBgColor(totalState,0.2f));
        subTableRow.setBackgroundColor(setBgColor(totalState,0.2f));
        subTableLowTop.setBackgroundColor(setBgColor(totalState,0.5f));

        setImageView(mainIcon, totalState);

        date_1.setText(WeekType.getCurrentWeekType());
        date_2.setText(WeekType.getCurrentWeekType());
        date_3.setText(WeekType.getCurrentWeekType());
        date_4.setText(WeekType.getCurrentWeekType());
        date_5.setText(WeekType.getCurrentWeekType());
        date_6.setText(WeekType.getCurrentWeekType());

        setImageView(time_1, Crime.TotalState(TimeRange.MIDNIGHT_0_4, currentWeekType, location));
        setImageView(time_2, Crime.TotalState(TimeRange.EARLY_MORNING_4_7, currentWeekType, location));
        setImageView(time_3, Crime.TotalState(TimeRange.MORNING_7_12, currentWeekType, location));
        setImageView(time_4, Crime.TotalState(TimeRange.AFTERNOON_12_18, currentWeekType, location));
        setImageView(time_5, Crime.TotalState(TimeRange.EVENING_18_20, currentWeekType, location));
        setImageView(time_6, Crime.TotalState(TimeRange.NIGHT_20_24, currentWeekType, location));
        Log.d("", "setExplainText: -------------------");
        cond_1.setText(Crime.TotalState(TimeRange.MIDNIGHT_0_4, currentWeekType, location));
        cond_2.setText(Crime.TotalState(TimeRange.EARLY_MORNING_4_7, currentWeekType, location));
        cond_3.setText(Crime.TotalState(TimeRange.MORNING_7_12, currentWeekType, location));
        cond_4.setText(Crime.TotalState(TimeRange.AFTERNOON_12_18, currentWeekType, location));
        cond_5.setText(Crime.TotalState(TimeRange.EVENING_18_20, currentWeekType, location));
        cond_6.setText(Crime.TotalState(TimeRange.NIGHT_20_24, currentWeekType, location));
        Log.d("", "setExplainText: -------------------");
    }

    public void setDateTimeAndLocation(String location) {
        if (locationTextView != null) {
            locationTextView.setText(location);
        }
        if (dateTimeTextView != null) {
            // 현재 시간에 10분 더하기
            Calendar calendar = Calendar.getInstance();
            // 날짜 포맷팅
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm a", Locale.getDefault());
            String currentDateTime = dateFormat.format(calendar.getTime());

            // 포맷팅된 날짜를 TextView에 설정
            dateTimeTextView.setText(currentDateTime);
        }


    }

    private TimeRange getCurrentTimeRange() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 4) {
            return TimeRange.MIDNIGHT_0_4;
        } else if (hour >= 4 && hour < 7) {
            return TimeRange.EARLY_MORNING_4_7;
        } else if (hour >= 7 && hour < 12) {
            return TimeRange.MORNING_7_12;
        } else if (hour >= 12 && hour < 18) {
            return TimeRange.AFTERNOON_12_18;
        } else if (hour >= 18 && hour < 20) {
            return TimeRange.EVENING_18_20;
        } else {
            return TimeRange.NIGHT_20_24;
        }
    }

    private WeekType getCurrentWeekType() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return WeekType.Mon;
            case Calendar.TUESDAY:
                return WeekType.Tue;
            case Calendar.WEDNESDAY:
                return WeekType.Wed;
            case Calendar.THURSDAY:
                return WeekType.Thu;
            case Calendar.FRIDAY:
                return WeekType.Fri;
            case Calendar.SATURDAY:
                return WeekType.Sat;
            case Calendar.SUNDAY:
                return WeekType.Sun;
            default:
                throw new IllegalArgumentException("Unknown day of week: " + dayOfWeek);
        }
    }
    private int setStatusColor(String status) {
        int color = Color.BLACK;
        switch (status) {
            case "매우좋음":
                color = Color.parseColor("#AEDEF0");
                break;
            case "매우나쁨":
                color = Color.parseColor("#FF0000");
                break;
            case "보통":
                color =  Color.parseColor("#33CC00");
                break;
            case "좋음":
                color = Color.parseColor("#33CCFF");
                break;
            case "나쁨":
                color = Color.parseColor("#CC6633");
                break;
        }
        return color;
    }
    private int setBgColor(String status, float val) {
        int color = setStatusColor(status);
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            // 명도(value) 감소
            hsv[2] = Math.max(0.0f, hsv[2] - val);  // 명도를 20% 감소시키되 최소값을 넘지 않도록 함
            color = Color.HSVToColor(hsv);
        return color;
    }


    private void setImageView(ImageView imageView, String status) {
        if (imageView != null) {
            switch (status) {
                case "매우좋음":
                    imageView.setImageResource(R.drawable.ic_g5);  // 매우좋음 상태에 대한 이미지 리소스 설정
                    break;
                case "좋음":
                    imageView.setImageResource(R.drawable.ic_g4);  // 좋음 상태에 대한 이미지 리소스 설정
                    break;
                case "보통":
                    imageView.setImageResource(R.drawable.ic_g3);  // 보통 상태에 대한 이미지 리소스 설정
                    break;
                case "나쁨":
                    imageView.setImageResource(R.drawable.ic_g2);  // 나쁨 상태에 대한 이미지 리소스 설정
                    break;
                case "매우나쁨":
                    imageView.setImageResource(R.drawable.ic_g1);  // 매우나쁨 상태에 대한 이미지 리소스 설정
                    break;
                default:
                    imageView.setImageResource(R.drawable.ic_g1);  // 기본 이미지 리소스 설정
                    break;
            }
        }
    }

    public interface AddressCallback {
        void onAddressFound(String address);
        void onError(String error);
    }
}
